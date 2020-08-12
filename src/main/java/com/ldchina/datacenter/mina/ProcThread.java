package com.ldchina.datacenter.mina;


import com.alibaba.fastjson.JSON;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.mina.protocol.protocols.lufft_comoncmd.LufftCommonCmd;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.*;
import com.ldchina.datacenter.utils.DbUtil;
import com.ldchina.datacenter.utils.TimeUtil;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
    String stationId = null;
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
        stationId = ioSessionToStationId.get(ioSession);
        //   System.out.println("ThreadName：" + Thread.currentThread().getName() + " RemoteAddr：" + ioSession.getRemoteAddress().toString());
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
                if (ioSessionToStationId.get(ioSession) != null &&
                        AppConfig.stationidTostationInfo
                                .get(ioSessionToStationId.get(ioSession)).webSocketSession != null) {
                    try {
                        AppConfig.stationidTostationInfo
                                .get(ioSessionToStationId.get(ioSession)).webSocketSession.getBasicRemote().sendText(msg);
                    } catch (IOException ex) {
                        log.error("Sent To WebSocket Failed:", ex);
                    }
                }
                if (msg.contains("<F>") || msg.contains("<N>") || msg.contains("/////")) {
                    DbUtil.dbMapperUtil.qxReuploadMapper.delete(stationId, AppConfig.stationidTostationInfo.get(stationId)._Runtime_LastReuploadTime);
                    doReupload(stationId);
                    return;
                }
                if (msg.length() > 8) {
                    if ((msg.charAt(0) == '#' && msg.charAt(4) == ',' && msg.charAt(7) == ',')) {
                        PROC_LMDS4_Data(msg);
                        doReupload(stationId);
                        return;
                    }
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
            AppConfig.stationidTostationInfo
                    .get(ioSessionToStationId.get(ioSession))
                    .stationState.commtime = new Date();
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
//        System.out.println("数据长度：" + lufftCommonCmd.datalen);
        System.out.println("源地址：" + lufftCommonCmd.srcId);
//        System.out.println("目的地址：" + lufftCommonCmd.distId);
//        System.out.println("数据信息通道数：" + lufftCommonCmd.channelNum);

        DataInfo dataInfo = new DataInfo();
        String val = "", key = "";
        dataInfo.stationid = String.valueOf(lufftCommonCmd.srcId);
        stationId = dataInfo.stationid;
        if (StatusNo.OK != checkStationId(dataInfo.stationid)) {
            System.out.println("未知站点号：" + dataInfo.stationid);
            return;
        }

        Set keySet = AppConfig.stationidTostationInfo.get(dataInfo.stationid).keyToWebconfig.keySet();

        switch (lufftCommonCmd.cmd) {
            case LufftCommonCmd.Cmd_MinUpload:
                byte[] rep = new byte[]{0x01, 0x00, 0x01, 0x00, 0x10, 0x18, 0x00, 0x01, 0x02, 0x00, 0x03, (byte) 0xBB, (byte) 0xBB, 0x04};
                ioSession.write(IoBuffer.wrap(rep));
                logString = "";
                String sqlDataTable1 = "replace into `data_" + dataInfo.stationid + "` ('";
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
                            dataInfo.obtime = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
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
                                    dataInfo.PS = String.valueOf((int) lufftCommonCmd.channelData[i + 3] / (float) channelInfo.scale);
                                    logString += channelInfo.name + ":" + dataInfo.PS + ";";
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
                procStationStatus(dataInfo.stationid, ioSession);
                insertData(dataInfo);
                if (AppConfig.stationidTostationInfo.get(dataInfo.stationid).dataInfo.obtime.before(dataInfo.obtime)) {
                    insertLatestData(dataInfo);
                }
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
            DataInfo dataInfo = new DataInfo();
            int posion = 4;
            String[] ss = s.split(",");
            dataInfo.stationid = ss[0].substring(1);
            stationId = dataInfo.stationid;
            if (StatusNo.OK != checkStationId(dataInfo.stationid)) {
                log.warn("未知站点号:" + dataInfo.stationid);
                return;
            }
            String logString = "站点:" + dataInfo.stationid + ";";
            dataInfo.obtime = TimeUtil.parseDate(ss[2], "yyyyMMddHHmm");
            logString += "时间:" + ss[2] + ";";
            for (int i = 0; i < ss[3].length(); i++) { // 根据报文内传感器类型字段确定有多少种传感器
                String mainKey = String.valueOf(ss[3].charAt(i));

                Map<String, ChannelInfo> subKeyToChannel = AppConfig.keyMainSubToChannelInfoByProtocol
                        .get("LMD-S4")
                        .get(mainKey);
                if (null != subKeyToChannel) {
                    Set subKeys = subKeyToChannel.keySet();
                    for (int j = 0; j < subKeys.size(); j++) {
                        if (subKeys.contains(ss[posion])) { 
                            ChannelInfo channelInfo = subKeyToChannel.get(ss[posion]);
                            String sqlK = "_" + mainKey + "_" + ss[posion];
                            if (AppConfig.stationidTostationInfo.get(dataInfo.stationid)
                                    .keyToWebconfig.keySet().contains(sqlK)) {
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
                            }
                            posion += 2;
                        } else {
                            break;
                        }
                    }
                }
            }
            if (ss[posion].equals("PS")) {
                dataInfo.PS = ss[posion + 1];
                logString += "电压" + ":" + dataInfo.PS + ";";
            }
            log.info(logString);
            procStationStatus(dataInfo.stationid, ioSession);
            insertData(dataInfo);
            long diff = 1; //默认为正常报文（新建站点时，stationState.obtime ==null）
            long dateOld = 0;
            if (AppConfig.stationidTostationInfo.get(dataInfo.stationid).stationState.obtime != null) {
                dateOld = AppConfig.stationidTostationInfo.get(dataInfo.stationid).stationState.obtime.getTime();
                diff = (dataInfo.obtime.getTime() - dateOld) / 1000 / 60;
            }
            if (diff < 0) {
                // 说明这条报文是补包的报文，需删除补包表即可，无须更新站点信息
                DbUtil.dbMapperUtil.qxReuploadMapper.delete(dataInfo.stationid, dataInfo.obtime);
            } else {
                /*数据和缓存比如果数据是新的则更新缓存，插入最新数据表*/
                insertLatestData(dataInfo);
                if (diff > 1) {
                    // 说明中间有断报
                    double insCount = (diff > 1440 * 31) ? (1440 * 31) : diff;// 最多补31天历史记录
                    Calendar obtime = Calendar.getInstance();// 用于插入时自增时间
                    obtime.setTime(new Date(dateOld));
                    for (int i = 1; i < insCount; i++) {
                        obtime.add(Calendar.MINUTE, 1);
                        DbUtil.dbMapperUtil.qxReuploadMapper.save(dataInfo.stationid, obtime.getTime());
                    }
                }
            }


        } catch (Exception ex) {
            log.error("LMDS4 DataProc Err:", ex);
            ex.printStackTrace();
        }
    }

    private void doReupload(String stationId) {
        if (stationId == null) return;
        Date reuploadDateTime = DbUtil.dbMapperUtil.qxReuploadMapper.getReupload(stationId);
        if (reuploadDateTime != null) {
            if (AppConfig.stationidTostationInfo.get(stationId)._Runtime_LastReuploadTime.equals(reuploadDateTime)) {
                AppConfig.stationidTostationInfo.get(stationId)._Runtime_ReuploadTryTimes++;
            } else {
                AppConfig.stationidTostationInfo.get(stationId)._Runtime_ReuploadTryTimes = 1;
            }
            if (AppConfig.stationidTostationInfo.get(stationId)._Runtime_ReuploadTryTimes > 2) {
                DbUtil.dbMapperUtil.qxReuploadMapper.delete(stationId, AppConfig.stationidTostationInfo.get(stationId)._Runtime_LastReuploadTime);
                AppConfig.stationidTostationInfo.get(stationId)._Runtime_ReuploadTryTimes = 0;
            } else {
                // LocalDateTime.
                AppConfig.stationidTostationInfo.get(stationId)._Runtime_LastReuploadTime = new Date(reuploadDateTime.getTime());
                String cmd = "DOWN " + TimeUtil.format(AppConfig.stationidTostationInfo.get(stationId)._Runtime_LastReuploadTime, "yyyyMMddHHmm") + " 1";
                log.info("Send Cmd To [" + stationId + "]: " + cmd);
                AppConfig.stationidTostationInfo.get(stationId).ioSession.write(cmd);
            }
        }
    }

    StatusNo checkStationId(String stationid) {
        if (!AppConfig.stationidTostationInfo.keySet().contains(stationid)) {
            return StatusNo.未知站点号;
        }
        return StatusNo.OK;
    }

    void procStationStatus(String stationid, IoSession ioSession) {
        AppConfig.stationidTostationInfo.get(stationid).stationState.commtime = new Date();
        AppConfig.stationidTostationInfo.get(stationid).ioSession = ioSession;
        ioSessionToStationId.put(ioSession, stationid);
    }
    /**
     * 插入数据库latest表，处理最新数据缓存
     *
     * @param dataInfo
     * @return
     */
    StatusNo insertLatestData(DataInfo dataInfo) {
        AppConfig.stationidTostationInfo.get(dataInfo.stationid).dataInfo = dataInfo;
        AppConfig.stationidTostationInfo.get(dataInfo.stationid).stationState.obtime = dataInfo.obtime;
        String sqlString = "MERGE INTO QX_LATEST ( `stationid`, `obtime`,`ps`, `data`) VALUES ( '" + dataInfo.stationid
                + "', '" + TimeUtil.format(dataInfo.obtime, "yyyy-MM-dd HH:mm:00") + "', '" + dataInfo.PS + "', '"
                + JSON.toJSONString(dataInfo.val) + "')";
        try {
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlString); // 插入数据
            sqlString = "update `qx_station` set obtime='"
                    + TimeUtil.format(dataInfo.obtime, "yyyy-MM-dd HH:mm:00")
                    + "' where stationid='"
                    + dataInfo.stationid
                    + "'";
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlString);
            return StatusNo.OK;
        } catch (Exception ex) {
            log.error("数据库错误【" + dataInfo.stationid + "】：", ex.getStackTrace()[0]);
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
        String sqlDataString1 = "merge into `data_" + dataInfo.stationid + "` ( `obtime`,`ps`";
        String sqlDataString2 = ") values ( '" + TimeUtil.format(dataInfo.obtime, "yyyy-MM-dd HH:mm:00") + "', '"
                + dataInfo.PS + "'";
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
