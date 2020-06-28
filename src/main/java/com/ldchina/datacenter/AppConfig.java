package com.ldchina.datacenter;

import javax.annotation.PostConstruct;


import com.ldchina.datacenter.dao.entity.StationInfo;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.utils.DbUtil;
import com.ldchina.datacenter.utils.JsonUtil;

import java.util.*;

@Component
@DependsOn("DbUtil")
public class AppConfig {

    public final static int IOSESSION_TIMEOUT_MIN = 5;

    /**
     * 测量要素数据库键对测量要素描述类
     */
    public static Map<String,Map<String,Map<String, ChannelInfo>>> keyMainSubToChannelInfoByProtocol = new HashMap<>();
    /**
     * 加载所有数据库站点
     */
    public static Map<String, StationStatus> stationidTostationStatus = new HashMap<>();
    /**
     * 根据站点号获取对应站点的所有显示配置
     */
    public static Map<String, Map<String, WebConfig>> keyToWebconfigByStationid = new HashMap<>();

    private final static Logger log = LoggerFactory.getLogger(AppConfig.class);
    /**
     * 支持传感器类型及其测量参数
     */
    public static Map<String, Sensor> sensorMap = new HashMap<String, Sensor>();
    public static List<Sensor> sensorList = new ArrayList<Sensor>();

    public static AppConfig appConfig;
    @Value("${webTitle}")
    public String webTitle;


    /**
     * 用于处理站点更新时间超时显示离线
     */
    private static Timer timer = new Timer();

    static {
        List<DataInfo> list = DbUtil.dbMapperUtil.latestDataMapper.getLatestDataAll();
        List<StationInfo> stationInfos = DbUtil.dbMapperUtil.qxStationMapper.getAllStations();
        if (stationInfos != null) {
            stationInfos.forEach(stationInfo -> {
                StationStatus stationStatus = new StationStatus(null, stationInfo);
                for (int j = 0; j < list.size(); j++) {
                    if (stationInfo.getSTATIONID().equals(list.get(j).STATIONID)) {
                        stationStatus.dataInfo = list.get(j);
                        System.out.println("初始化缓存【" + stationStatus.stationInfo.alias + "】更新时间【" + stationStatus.stationInfo.OBTIME + "】");
                        list.remove(j);
                        break;
                    }
                }
                if(stationStatus.dataInfo==null){
                    stationStatus.dataInfo = new DataInfo(stationInfo.getSTATIONID());
                }
                stationidTostationStatus.put(stationInfo.getSTATIONID(), stationStatus);
            });
        }
        /**
         * 延迟100ms后，间隔1s打印出：hello world
         *
         * @param args
         * @throws InterruptedException
         */
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long ms = new Date().getTime();
                for (Map.Entry<String, StationStatus> entry : stationidTostationStatus.entrySet()) {
                    if (Math.abs((entry.getValue().stationInfo.commtime.getTime() - ms) / 1000 / 60) > IOSESSION_TIMEOUT_MIN) {
                        if(entry.getValue().ioSession!=null){
                            entry.getValue().ioSession.close(true);
                            System.out.println("Time Out:"+entry.getKey());
                        }

                    }
                }
            }
        }, 20000, 20000);
    }


    @PostConstruct
    public void init() {
        List<String> pathList = JsonUtil.getDirectories("./config/protocol");
        pathList.forEach((path) -> {
            //遍历所有协议文件夹
            String protocol = path.substring(path.lastIndexOf("\\")+1);
         //   List<String> subPathList = JsonUtil.getDirectories(path);
            Map<String,Map<String, ChannelInfo>> mainchToSubMap = new LinkedHashMap<>();

         //   subPathList.forEach((subpath) -> {
                //遍历每个协议文件夹里面的文件
                sensorList = JsonUtil.getAllJavaObjInDir(path, Sensor.class);
                sensorList.forEach(s -> {
                    //遍历每个文件里面的协议
                    for (Map.Entry<String, Map<String, ChannelInfo>> entry : s.channel.entrySet()) {
                        //遍历子协议ChannelInfo中的MainId
                        sensorMap.put(entry.getKey(), s);
                        Map<String, ChannelInfo> subIdToChannelInfo = new LinkedHashMap<>();

                        Map<String, ChannelInfo> channelInfoMap = entry.getValue();
                        for (Map.Entry<String, ChannelInfo> channelInfoEntry : channelInfoMap.entrySet()) {
                            //遍历所有子协议SubId
                            ChannelInfo channelInfo = channelInfoEntry.getValue();
                            channelInfo.key = "_" + entry.getKey() + "_" + channelInfoEntry.getKey();
                            channelInfo._SensorName = s.name;
                            subIdToChannelInfo.put(channelInfoEntry.getKey(), channelInfo);
                        }//结束遍历所有子协议SubId
                        mainchToSubMap.put(entry.getKey(),subIdToChannelInfo);
                    }//结束遍历子协议ChannelInfo中的MainId
                });//结束遍历每个文件里面的协议
        //    });//结束遍历每个协议文件夹里面的文件
            keyMainSubToChannelInfoByProtocol.put(protocol, mainchToSubMap);
        }); //结束遍历协议文件夹


        /**
         * 获取web站点显示配置
         */
        List<WebConfig> webconfigs = DbUtil.dbMapperUtil.webConfigMapper.selectAll();
        for (int i = 0; i < webconfigs.size(); i++) {
            WebConfig webconfig = webconfigs.get(i);
            if (keyToWebconfigByStationid.get(webconfig.stationid) == null) {
                keyToWebconfigByStationid.put(webconfig.stationid, new HashMap<>());
            }
            keyToWebconfigByStationid.get(webconfig.stationid).put(webconfig.key, webconfig);
        }
        Layui.initListCols();
        appConfig = this;
    }
}
