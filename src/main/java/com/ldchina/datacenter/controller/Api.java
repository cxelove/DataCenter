package com.ldchina.datacenter.controller;

import com.alibaba.fastjson.JSONObject;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.StationInfo;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.mina.UpdateBin;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.DataInfo;
import com.ldchina.datacenter.types.Layui;
import com.ldchina.datacenter.types.StationStatus;
import com.ldchina.datacenter.utils.DbUtil;
import com.ldchina.datacenter.utils.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/api")
public class Api {
    private final static Logger log = LoggerFactory.getLogger(Api.class);

    /**
     * 获取站点名称 layui table 用
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getStations")
    public Layui getStations() {
        List<StationInfo> stationInfos = DbUtil.dbMapperUtil.qxStationMapper.getAllStations();
        return Layui.data(null, stationInfos.size(), stationInfos);
    }

    /**
     * 获取站点的经纬度
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getMapLng")
    public List<StationInfo> getMapLng() {
        List<StationInfo> list = new ArrayList();
        for (Map.Entry<String, StationStatus> entry : AppConfig.stationidTostationStatus.entrySet()) {
            StationInfo x = entry.getValue().stationInfo;
            if (x == null || x.getLat() == null || x.getLng() == null) {
                continue;
            } else {
                list.add(x);
            }
        }
        return list;
    }

    /**
     * 获取站点最新传感器数据
     *
     * @return 返回最新数据
     */
    @ResponseBody
    @RequestMapping("/getLatestById")
    public Map getLatestById(String stationId) {
        if (stationId == null)
            return null;
        //JSONObject jsonObject = n
        Map<String, Object> map = new HashMap<>();
        map.put("data", AppConfig.stationidTostationStatus.get(stationId).dataInfo);
        List<ChannelInfo> list = new ArrayList<>();
//		for( AppConfig.stationidTostationStatus.get(stationId).stationInfo)
//			AppConfig.
		String protocol = AppConfig.stationidTostationStatus.get(stationId).stationInfo.protocol;
		for(Map.Entry<String, WebConfig> webConfigEntry:AppConfig.keyToWebconfigByStationid.get(stationId).entrySet()){
			String mainId = webConfigEntry.getKey().split("_")[1];
			String subId = webConfigEntry.getKey().split("_")[2];
			if(webConfigEntry.getValue().mapDisplay){
				list.add(AppConfig.keyMainSubToChannelInfoByProtocol.get(protocol).get(mainId).get(subId));
			}
		}

//        for (Map.Entry<String, Map<String, ChannelInfo>> mapEntry : AppConfig.keyMainSubToChannelInfoByProtocol.get(AppConfig.stationidTostationStatus.get(stationId).stationInfo.protocol).entrySet()) {
//            for (Map.Entry<String, ChannelInfo> channelInfoEntry : mapEntry.getValue().entrySet()) {
//                if (AppConfig.keyToWebconfigByStationid.get(channelInfoEntry.getValue().key).)
//            }
//        }
        String js = JSONObject.toJSONString(list);
        map.put("title",js);
        return map;
    }

    /**
     * 获取站点状态
     */
    @ResponseBody
    @RequestMapping("/getSta")
    public Map<String, String> getSta() {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, StationStatus> entry : AppConfig.stationidTostationStatus.entrySet()) {
            map.put(entry.getKey(), (entry.getValue().ioSession==null)?"STATION_OFFLINE":"STATION_ONLINE");
        }
        return map;
    }

    /**
     * 获取站点一天数据
     *
     * @param stationId 站点号
     * @param date      日期
     * @return
     */
    @ResponseBody
    @RequestMapping("/getOnedayLimit")
    public Layui getOneday(String stationId, String date, Integer limit, Integer page) {
        if (stationId == null || date == null || limit == null || page == null)
            return Layui.data(null, 0, null);

        if (DbUtil.dbMapperUtil.iSqlMapper.isTableExit("DATA_M_" + stationId)) {
            String sqlString = "SELECT * FROM DATA_M_" + stationId + " WHERE DATEDIFF(day,obTime,'" + date
                    + "')=0 ORDER BY obTime DESC LIMIT " + limit * (page - 1) + " , " + limit;
            List<Map<String, Object>> resultList = DbUtil.dbMapperUtil.iSqlMapper.sqlget(sqlString);
            sqlString = "SELECT COUNT(*) FROM DATA_M_" + stationId + " WHERE DATEDIFF(day,obTime,'" + date + "')=0";
            List<Map<String, Object>> countList = DbUtil.dbMapperUtil.iSqlMapper.sqlget(sqlString);
            return Layui.data(null, ((Long) countList.get(0).get("COUNT(*)")).intValue(), resultList);
        } else
            return Layui.data(null, 0, null);
    }

    @ResponseBody
    @RequestMapping("/startUpdate")
    public Map<String, String> updateBin(String stationId, HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
       // Sessions sessions = MinaTcpServerHandler.sessionsMap.get(stationId);
        if (AppConfig.stationidTostationStatus
                .get(stationId).ioSession != null) {
            if (AppConfig.stationidTostationStatus
                    .get(stationId).updateBin != null) {
                map.put("msg", "站点忙");
            } else {
                String path = new ApplicationHome(getClass()).getSource().getParentFile().toString() + "\\upload\\bin";
                AppConfig.stationidTostationStatus
                        .get(stationId).updateBin = new UpdateBin();
                AppConfig.stationidTostationStatus
                        .get(stationId).updateBin.openUpdate(stationId, path);
                AppConfig.stationidTostationStatus
                        .get(stationId).updateBin.start();
                map.put("msg", "0");
            }
        } else {
            map.put("msg", "站点未上线");
        }
        return map;
    }

    /**
     * 获取所有站点最新数据，列表展示
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getAllLatest")
    public Layui getAllLatestByMeasure(String measure) {
        List<Map<String, Object>> listsList = new ArrayList<Map<String, Object>>();
        for (Map.Entry<String, StationStatus> entry : AppConfig.stationidTostationStatus.entrySet()) {
            if (!entry.getValue().stationInfo.measure.equals(measure)) continue;
            Map<String, Object> map = new HashMap<String, Object>();
            DataInfo dataInfo = entry.getValue().dataInfo;
            map.put("stationid", dataInfo.stationId);
            map.put("obtime", dataInfo.obTime);
            map.put("ps", dataInfo.ps);
            for (Map.Entry<String, String> mp : dataInfo.val.entrySet()) {
                map.put(mp.getKey(), mp.getValue());
            }
            listsList.add(map);
        }
        return Layui.data(measure, listsList.size(), listsList);
    }

    class ReportData {
        public String stationid;
        public String alias;
        public Long count;
        public String percent;

        public ReportData(String stationId, String name) {
            this.stationid = stationId;
            this.alias = name;
        }
    }

    /**
     * 获取统计信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("getReport")
    public Layui getReport(String startTime, String endTime) {
        if (startTime == null && endTime == null)
            return Layui.data(null, 0, null);

        List<ReportData> reportDataList = new ArrayList<>();
        int full = 1;
        Date now = new Date();

        try {
            Date fromDate = TimeUtil.parseDate(startTime + " 00:00", "yyyy-MM-dd HH:mm");
            Date toDate = TimeUtil.parseDate(endTime + " 00:00", "yyyy-MM-dd HH:mm");
            if (toDate.before(fromDate)) {
                full = (int) (toDate.getTime() / 1000 / 60 - fromDate.getTime() / 1000 / 60) + 24 * 60;
            } else {
                full = (int) (now.getTime() / 1000 / 60 - fromDate.getTime() / 1000 / 60);
            }
            String sqlString = "";
            // full /=60;
            if (full > 0) {
                for (Map.Entry<String, StationStatus> entry : AppConfig.stationidTostationStatus.entrySet()) {
                    DataInfo dataInfo = entry.getValue().dataInfo;
                    if (DbUtil.dbMapperUtil.iSqlMapper.isTableExit("DATA_M_" + dataInfo.stationId)) {
                        ReportData reportData = new ReportData(dataInfo.stationId,
                                AppConfig.stationidTostationStatus.get(dataInfo.stationId).stationInfo.getAlias());

                        sqlString = "SELECT COUNT(*) FROM DATA_M_" + dataInfo.stationId
                                + " WHERE obtime < DATEADD(dd,1,'" + endTime + "') AND obTime >= '" + startTime + "'";
                        reportData.count = (Long) DbUtil.dbMapperUtil.iSqlMapper.sqlget(sqlString).get(0)
                                .get("COUNT(*)");
                        reportData.percent = Double.toString(Math.round(100.0 * reportData.count / full * 100) / 100.0)
                                + "%";
                        reportDataList.add(reportData);
                    }

                }
                return Layui.data(null, reportDataList.size(), reportDataList);
            } else {
                return Layui.data(null, 0, null);
            }
        } catch (Exception e) {
            log.error("解析:", e);
        }
        return Layui.data(null, 0, null);
    }
}
