package com.ldchina.datacenter.controller;

import com.alibaba.fastjson.JSONObject;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.StationState;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.mina.UpdateBin;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.DataInfo;
import com.ldchina.datacenter.types.Layui;
import com.ldchina.datacenter.types.StationInfo;
import com.ldchina.datacenter.utils.DbUtil;
import com.ldchina.datacenter.utils.TimeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
        List<StationState> stationStates = DbUtil.dbMapperUtil.qxStationMapper.getAllStations();
        return Layui.data(null, stationStates.size(), stationStates);
    }

    /**
     * 获取站点的经纬度
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/getMapLng")
    public List<StationState> getMapLng() {
        List<StationState> list = new ArrayList();
        for (Map.Entry<String, StationInfo> entry : AppConfig.stationidTostationInfo.entrySet()) {
            StationState x = entry.getValue().stationState;
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
        Map<String, Object> map = new HashMap<>();
        map.put("data", AppConfig.stationidTostationInfo.get(stationId).dataInfo);
        List<ChannelInfo> list = new ArrayList<>();
        String protocol = AppConfig.stationidTostationInfo.get(stationId).stationState.protocol;
        for (Map.Entry<String, WebConfig> webConfigEntry : AppConfig.stationidTostationInfo.get(stationId).keyToWebconfig.entrySet()) {
            String mainId = webConfigEntry.getKey().split("_")[1];
            String subId = webConfigEntry.getKey().split("_")[2];
            if (webConfigEntry.getValue().mapDisplay) {
                list.add(AppConfig.keyMainSubToChannelInfoByProtocol.get(protocol).get(mainId).get(subId));
            }
        }
        String js = JSONObject.toJSONString(list);
        map.put("title", js);
        return map;
    }

    @RequestMapping(value = "/updateWebConfigByStationId", method = RequestMethod.POST)
    @ResponseBody
    public int updateWebConfigByStationId(String stationId, HttpServletRequest request) {
        String ds = request.getParameter("postData");
        JSONObject json = JSONObject.parseObject(ds); //使用net.sf.json.JSONObject对象来解析json
        Set valKeySet = json.keySet();
        String sql = "";
        Map<String, WebConfig> retMap = new HashMap<>();
        try {
            for (Map.Entry<String, WebConfig> webConfigEntry : AppConfig.stationidTostationInfo.get(stationId).keyToWebconfig.entrySet()) {
                if (valKeySet.contains(webConfigEntry.getKey())) {
                    AppConfig.stationidTostationInfo.get(stationId).keyToWebconfig.get(webConfigEntry.getKey()).mapDisplay = true;
                    sql = "UPDATE `web_config` SET `mapdisplay` = true WHERE `key`='" + webConfigEntry.getKey() + "' AND `stationid` = '" + stationId + "'";
                } else {
                    AppConfig.stationidTostationInfo.get(stationId).keyToWebconfig.get(webConfigEntry.getKey()).mapDisplay = false;
                    sql = "UPDATE `web_config` SET `mapdisplay` = false WHERE `key`='" + webConfigEntry.getKey() + "' AND `stationid` = '" + stationId + "'";
                }
                DbUtil.dbMapperUtil.iSqlMapper.sqlput(sql);
            }
            return 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return 1;
        }
    }

    /**
     * 获取站点状态
     */
    @ResponseBody
    @RequestMapping("/getSta")
    public Map<String, String> getSta() {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, StationInfo> entry : AppConfig.stationidTostationInfo.entrySet()) {
            map.put(entry.getKey(), (entry.getValue().ioSession == null) ? "STATION_OFFLINE" : "STATION_ONLINE");
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

        if (DbUtil.dbMapperUtil.iSqlMapper.isTableExit("DATA_" + stationId)) {
            String sqlString = "SELECT * FROM DATA_" + stationId + " WHERE DATEDIFF(day,obTime,'" + date
                    + "')=0 ORDER BY obTime DESC LIMIT " + limit * (page - 1) + " , " + limit;
            List<Map<String, Object>> resultList = DbUtil.dbMapperUtil.iSqlMapper.sqlget(sqlString);
            sqlString = "SELECT COUNT(*) FROM DATA_" + stationId + " WHERE DATEDIFF(day,obTime,'" + date + "')=0";
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
        if (AppConfig.stationidTostationInfo
                .get(stationId).ioSession != null) {
            if (AppConfig.stationidTostationInfo
                    .get(stationId).updateBin != null) {
                map.put("msg", "站点忙");
            } else {
                String path = new ApplicationHome(getClass()).getSource().getParentFile().toString() + "\\upload\\bin";
                AppConfig.stationidTostationInfo
                        .get(stationId).updateBin = new UpdateBin();
                AppConfig.stationidTostationInfo
                        .get(stationId).updateBin.openUpdate(stationId, path);
                AppConfig.stationidTostationInfo
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
        for (Map.Entry<String, StationInfo> entry : AppConfig.stationidTostationInfo.entrySet()) {
            if (!entry.getValue().stationState.measure.equals(measure)) continue;
            Map<String, Object> map = new HashMap<String, Object>();
            DataInfo dataInfo = entry.getValue().dataInfo;
            map.put("STATIONID", dataInfo.stationid);
            map.put("OBTIME", dataInfo.obtime);
            map.put("PS", dataInfo.PS);
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
        long full = 1;
        Date nowDate = new Date();
        try {
            Date fromDate = TimeUtil.parseDate(startTime + " 00:00", "yyyy-MM-dd HH:mm");
            Date toDate = TimeUtil.parseDate(endTime + " 00:00", "yyyy-MM-dd HH:mm");

            if((nowDate.getTime()-toDate.getTime())/1000 > 24 *3600) {
                if (fromDate.before(toDate)) {
                    full = (long) (toDate.getTime() - fromDate.getTime()) / 1000 / 60 + 24 * 60;
                } else {
                    full = 1440;
                }
            }else {
                full = (long) (nowDate.getTime() - fromDate.getTime()) / 1000 / 60;
            }

            if (full > 0) {
                List<String> list = new ArrayList<>();
                List<Map.Entry<String, StationInfo>> mapList = new ArrayList<Map.Entry<String, StationInfo>>(AppConfig.stationidTostationInfo.entrySet());
                Collections.sort(mapList, new Comparator<Map.Entry<String, StationInfo>>() {
                    public int compare(Map.Entry<String, StationInfo> status1, Map.Entry<String, StationInfo> status2) {
                        //降序排列
                        return status1.getValue().stationState.protocol.compareTo(status2.getValue().stationState.protocol);
                    }
                });
                for(int i=0;i<mapList.size();i++){
                    DataInfo dataInfo = mapList.get(i).getValue().dataInfo;
                    if (DbUtil.dbMapperUtil.iSqlMapper.isTableExit("DATA_" + dataInfo.stationid)) {
                        ReportData reportData = new ReportData(dataInfo.stationid,
                                AppConfig.stationidTostationInfo.get(dataInfo.stationid).stationState.getAlias());

                        String sqlString = "SELECT COUNT(*) FROM DATA_" + dataInfo.stationid
                                + " WHERE obtime < DATEADD(dd,1,'" + endTime + "') AND obTime >= '" + startTime + "'";
                        reportData.count = (Long) DbUtil.dbMapperUtil.iSqlMapper.sqlget(sqlString).get(0)
                                .get("COUNT(*)");
                        reportData.percent = Double.toString(Math.round(100.0 * reportData.count / full * 100) / 100.0)
                                + "%";
                        reportDataList.add(reportData);
                    }
                }
            return Layui.data(null, reportDataList.size(), reportDataList);
        } else{
            return Layui.data(null, 0, null);
        }
    } catch(
    Exception e)

    {
        log.error("解析:", e);
    }
        return Layui.data(null,0,null);
}
}
