package com.ldchina.datacenter.mina;


import com.alibaba.fastjson.JSON;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.StationInfo;
import com.ldchina.datacenter.mina.protocol.protocols.lufft_comoncmd.LufftCommonCmd;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.*;
import com.ldchina.datacenter.utils.DbUtil;
import com.ldchina.datacenter.utils.TimeUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

enum BUFF_TYPE {
    BUFF_BYTE,
    BUFF_ASCII
}

public class ProcThread implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(ProcThread.class);

    public static Map<IoSession, String> ioSessionToStationId = new HashMap<>();

    private IoSession ioSession;
    private Object buff = null;
    private BUFF_TYPE buffType = null;
    //创建线程池
    public static ExecutorService procCachedThreadPool = Executors.newCachedThreadPool();

    public ProcThread(IoSession ioSession, Object object) {
        this.ioSession = ioSession;
        this.buff = object;
        if (object instanceof byte[]) {
            this.buffType = BUFF_TYPE.BUFF_BYTE;
        } else if (object instanceof String) {
            this.buffType = BUFF_TYPE.BUFF_ASCII;
        }
    }

    @Override
    public void run() {
        System.out.println("ThreadName：" + Thread.currentThread().getName() + " RemoteAddr：" + ioSession.getRemoteAddress().toString());
        switch (buffType) {
            case BUFF_BYTE:
                byte[] bytes = (byte[]) buff;
                 /*
        富奥通采集器心跳包处理
         */
                if (bytes.length == 22
                        && bytes[0] == 0x7b
                        && bytes[bytes.length - 1] == 0x7b) {
                    PROC_FRT_HeartAck(bytes);
                    return;
                }
        /*
        富奥通采集器数据处理
         */
                if (bytes[0] == 0x01
                        && bytes[8] == 0x02
                        && bytes[bytes.length - 4] == 0x03
                        && bytes[bytes.length - 1] == 0x04) {
                    PROC_FRT_Data(bytes);
                    return;
                }
                break;
            case BUFF_ASCII:
                 /*
        下面当作Ascii解析
         */
                String msg = (String) buff;
                if ((msg.charAt(0) == '#' && msg.charAt(4) == ',' && msg.charAt(7) == ',')) {
                    PROC_LMDS4_Data(msg);
                }
                if (msg.contains("<F>") || msg.contains("<N>") || msg.contains("/////")) {

                }
                break;
        }


    }

    /**
     * 处理富奥通采集器心跳包
     */
    private void PROC_FRT_HeartAck(byte[] bytes) {
        byte[] bs = new byte[11];
        System.arraycopy(bytes, 4, bs, 0, 11);
        System.out.println("Frt HeartAck.");
        byte[] rep = new byte[16];
        rep[0] = 0x7b;
        rep[1] = (byte) 0x81;
        rep[2] = 0x00;
        rep[3] = 0x10;
        System.arraycopy(bytes, 4, rep, 4, 11);
        rep[15] = 0x7b;
        ioSession.write(IoBuffer.wrap(rep));
        if (ioSessionToStationId.get(ioSession) != null) {
            AppConfig.stationidTostationStatus
                    .get(ioSessionToStationId.get(ioSession))
                    .stationInfo.commtime = new Date();
        }
    }

    /**
     * 处理富奥通采集器数据
     */
    private void PROC_FRT_Data(byte[] bytes) {
        String logString = "";
        for (int i = 0; i < bytes.length; i++) {
            logString += String.format("%02X ", bytes[i] & 0xff);
        }
        log.info(logString);
        LufftCommonCmd lufftCommonCmd = new LufftCommonCmd(bytes, bytes.length);
        if (!lufftCommonCmd.checkAndParse()) {
            System.out.println("校验错误.");
            return;
        }
        System.out.println("命令：" + lufftCommonCmd.cmd);
        System.out.println("数据长度：" + lufftCommonCmd.datalen);
        System.out.println("源地址：" + lufftCommonCmd.srcId);
        System.out.println("目的地址：" + lufftCommonCmd.distId);
        System.out.println("数据信息通道数：" + lufftCommonCmd.channelNum);

        DataInfo dataInfo = new DataInfo();
        String val = "", key = "";
        dataInfo.stationId = String.valueOf(lufftCommonCmd.srcId);
        if (StatusNo.OK != checkStationId(dataInfo.stationId)) {
            System.out.println("未知站点号：" + dataInfo.stationId);
            return;
        }

        Set keySet = AppConfig.keyToWebconfigByStationid.get(dataInfo.stationId).keySet();

        switch (lufftCommonCmd.cmd) {
            case LufftCommonCmd.Cmd_MinUpload:
                byte[] rep = new byte[]{0x01, 0x00, 0x01, 0x00, 0x10, 0x18, 0x00, 0x01, 0x02, 0x00, 0x03, (byte) 0xBB, (byte) 0xBB, 0x04};
                ioSession.write(IoBuffer.wrap(rep));
                logString = "";
                String sqlDataTable1 = "replace into `data_" + dataInfo.stationId + "` ('";
                String sqlLatestTable2 = " values('";
                String sqlLatestTable = "";

                for (int i = 0; i < lufftCommonCmd.channelData.length - 1; ) {
                    int mainCh = lufftCommonCmd.channelData[i] & 0xff;
                    int subCh = lufftCommonCmd.channelData[i + 1] & 0xff;
                    key = "_" + String.format("%02X", mainCh) + "_" + String.format("%02X", subCh);
                    ChannelInfo channelInfo = AppConfig.keyMainSubToChannelInfoByProtocol
                            .get("FRT-MODBUS")
                            .get(String.format("%02X", mainCh))
                            .get(String.format("%02X", subCh));
                    switch (channelInfo.type) {
                        case 16/*日期类型*/:
                            String obTimeString = "20" +
                                    String.format("%02X", lufftCommonCmd.channelData[i + 3]) + "-" +
                                    String.format("%02X", lufftCommonCmd.channelData[i + 4]) + "-" +
                                    String.format("%02X", lufftCommonCmd.channelData[i + 5]) + " " +
                                    String.format("%02X", lufftCommonCmd.channelData[i + 6]) + ":" +
                                    String.format("%02X", lufftCommonCmd.channelData[i + 7]) + ":" +
                                    String.format("%02X", lufftCommonCmd.channelData[i + 8]);
                            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-M-d H:m:s");
                            LocalDateTime ldt = LocalDateTime.parse(obTimeString, dtf);
                            logString += channelInfo.name + ":" + dtf.format(ldt) + ";";
                            dataInfo.obTime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
                            break;
                        case 4/**/:
                            val = Float.toString(bytesToShort(lufftCommonCmd.channelData, i + 3) / (float) channelInfo.scale);
                            if (keySet.contains(key)) dataInfo.val.put(key, val);
                            logString += channelInfo.name + ":" + val + ";";
                            break;
                        case 1:
                            switch (subCh) {
                                case 1:
                                    //系统状态
                                    logString += channelInfo.name + ":" + lufftCommonCmd.channelData[i + 3] + ";";
                                    break;
                                case 2:
                                    //电压
                                    dataInfo.ps = String.valueOf((int) lufftCommonCmd.channelData[i + 3] / (float) channelInfo.scale);
                                    logString += channelInfo.name + ":" + dataInfo.ps + ";";
                                    break;
                            }
                            break;
                        case 3:
                            val = Float.toString(bytesToUShort(lufftCommonCmd.channelData, i + 3) / (float) channelInfo.scale);
                            if (keySet.contains(key)) dataInfo.val.put(key, val);
                            logString += channelInfo.name + ":" + val + ";";
                            break;
                    }

                    i = i + channelInfo.len + 3;
                }
                log.info(logString);
                procStationStatus(dataInfo.stationId, ioSession);
                procDatabase(dataInfo);
                break;
            case LufftCommonCmd.Cmd_RequestTime:
                break;
            case LufftCommonCmd.Cmd_Response:
                break;
            case LufftCommonCmd.Cmd_Reset:
                break;
            case LufftCommonCmd.Cmd_SetOrGetServerIp:
                break;
            case LufftCommonCmd.Cmd_SetOrGetServerPort:
                break;
            case LufftCommonCmd.Cmd_SetStationNo:
                break;
            case LufftCommonCmd.Cmd_SetAutoSendTime:
                break;
            case LufftCommonCmd.Cmd_RequestHistory:
                break;
        }
    }

    /**
     * 报文解析
     *
     * @param s
     * @return 状态码
     */
    private void PROC_LMDS4_Data(String s) {
        try {
            if (ioSessionToStationId.get(ioSession) != null &&
                    AppConfig.stationidTostationStatus
                            .get(ioSessionToStationId.get(ioSession)).webSocketSession != null) {
                AppConfig.stationidTostationStatus
                        .get(ioSessionToStationId.get(ioSession)).webSocketSession.getBasicRemote().sendText(s);
            }

            DataInfo dataInfo = new DataInfo();

            int posion = 4;
            String[] ss = s.split(",");
            dataInfo.stationId = ss[0].substring(1);

            if (StatusNo.OK != checkStationId(dataInfo.stationId)) {
                log.warn("未知站点号：" + dataInfo.stationId);
                return;
            }
            String logString = "站点：" + dataInfo.stationId + ";";
            dataInfo.obTime = TimeUtil.parseDate(ss[2], "yyyyMMddHHmm");
            logString += "时间：" + ss[2] + ";";
            for (int i = 0; i < ss[3].length(); i++) { // 根据报文内传感器类型字段确定有多少种传感器
                String mainKey = String.valueOf(ss[3].charAt(i));
                Map<String, ChannelInfo> subKeyToChannel = AppConfig.keyMainSubToChannelInfoByProtocol
                        .get("LMD-S4")
                        .get(mainKey);
                if (null != subKeyToChannel) {
                    Set subKeys = subKeyToChannel.keySet();
                    for (int j = 0; j < subKeys.size(); i++) {
                        if (subKeys.contains(ss[posion])) {
                            ChannelInfo channelInfo = subKeyToChannel.get(ss[posion]);
                            String sqlK = "_" + mainKey + "_" + ss[posion];
                            String sqlV = null;

                            if (channelInfo.mapcode != null) {
                                sqlV = channelInfo.mapcode.get(ss[posion + 1]);
                                if (sqlV == null) {
                                    sqlV = ss[posion + 1];
                                }
                            } else {
                                sqlV = ss[posion + 1];
                            }
                            logString += channelInfo.name + ":" + sqlV + ";";
                            dataInfo.val.put(sqlK, sqlV);
                            posion += 2;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (ss[posion].equals("PS")) {
                dataInfo.ps = ss[posion + 1];
                logString += "电压" + ":" + dataInfo.ps + ";";
            }
            log.info(logString);
            procStationStatus(dataInfo.stationId, ioSession);
            procDatabase(dataInfo);
        } catch (Exception ex) {
            log.error("LMDS4 DataProc Err:", ex.getMessage());
            ex.printStackTrace();
        }
    }

    StatusNo checkStationId(String stationid) {
        if (!AppConfig.keyToWebconfigByStationid.keySet().contains(stationid)) {
            return StatusNo.未知站点号;
        }
        return StatusNo.OK;
    }

    void procStationStatus(String stationid, IoSession ioSession) {
        AppConfig.stationidTostationStatus.get(stationid).stationInfo.commtime = new Date();
        AppConfig.stationidTostationStatus.get(stationid).ioSession = ioSession;
        ioSessionToStationId.put(ioSession, stationid);
    }

    /**
     * @param dataInfo
     */
    void procDatabase(DataInfo dataInfo) {
        StatusNo sn = insertData(dataInfo);
        System.out.println("插入数据：" + sn);
        if (sn != StatusNo.OK) return;
        /*数据和缓存比如果数据是新的则更新缓存，插入最新数据表*/
        if (AppConfig.stationidTostationStatus.get(dataInfo.stationId).dataInfo.obTime.before(dataInfo.obTime)) {
            System.out.println("更新缓存：" + insertLatestData(dataInfo));
        }
    }

    /**
     * 插入数据库latest表，处理最新数据缓存
     *
     * @param dataInfo
     * @return
     */
    StatusNo insertLatestData(DataInfo dataInfo) {
        AppConfig.stationidTostationStatus.get(dataInfo.stationId).dataInfo = dataInfo;
        AppConfig.stationidTostationStatus.get(dataInfo.stationId).stationInfo.obtime = dataInfo.obTime;
        String sqlString = "MERGE INTO QX_LATEST ( `stationid`, `obtime`,`ps`, `data`) VALUES ( '" + dataInfo.stationId
                + "', '" + TimeUtil.format(dataInfo.obTime, "yyyy-MM-dd HH:mm:00") + "', '" + dataInfo.ps + "', '"
                + JSON.toJSONString(dataInfo.val) + "')";
        try {
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlString); // 插入数据
            sqlString = "update `qx_station` set obtime='"
                    + TimeUtil.format(dataInfo.obTime, "yyyy-MM-dd HH:mm:00")
                    + "' where stationid='"
                    + dataInfo.stationId
                    + "'";
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlString);
            return StatusNo.OK;
        } catch (Exception ex) {
            log.error("数据库错误【" + dataInfo.stationId + "】：", ex.getStackTrace()[0]);
            return StatusNo.数据库错误;
        }
    }

    /**
     * 插入数据库data表
     *
     * @param dataInfo
     * @return
     */
    StatusNo insertData(DataInfo dataInfo) {
        String sqlDataString1 = "merge into `data_" + dataInfo.stationId + "` ( `obtime`,`ps`";
        String sqlDataString2 = ") values ( '" + TimeUtil.format(dataInfo.obTime, "yyyy-MM-dd HH:mm:00") + "', '"
                + dataInfo.ps + "'";
        for (Map.Entry<String, String> entry : dataInfo.val.entrySet()) {
            sqlDataString1 += " ,`" + entry.getKey() + "`";
            sqlDataString2 += " ,'" + entry.getValue() + "'";
        }
        String sqlDataString = sqlDataString1
                + sqlDataString2 + ")";
        try {
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlDataString); // 插入数据
            return StatusNo.OK;
        } catch (Exception e) {
            // TODO: handle exception
            // log.error(stationId + ":" + stationId, e);
            return StatusNo.数据库错误;
        }
    }

    /**
     * 将byte数组转换为整数
     *
     * @param bs
     * @return
     */
    public int bytesToShort(byte[] bs, int start) {
        return (bs[start] << 8) | (bs[start + 1] & 0xFF);
    }

    public int bytesToUShort(byte[] bs, int start) {
        return (bs[start] & 0xFF) << 8 | (bs[start + 1] & 0xFF);
    }
}
