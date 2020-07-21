package com.ldchina.datacenter.controller;

import com.alibaba.fastjson.JSONObject;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.StationState;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.DataInfo;
import com.ldchina.datacenter.types.Layui;
import com.ldchina.datacenter.types.StationInfo;
import com.ldchina.datacenter.utils.DbUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

@Controller
public class CreateStationController {

    @RequestMapping("/newstation")
    ModelAndView createStation(){
        ModelAndView mav = new ModelAndView("newstation");
        List<String> protocl = new ArrayList<>();
        Set protocolKeySet = AppConfig.keyMainSubToChannelInfoByProtocol.keySet();
        protocolKeySet.forEach(pcl->{
            protocl.add((String)pcl);
        });
        mav.addObject("protocol",protocl);
        return mav;
    }
    @RequestMapping("/api/getMeasureByProtocol")
    @ResponseBody
    private JSONObject getMeasureByProtocol(String protocol){
       // Map<String, Boolean> map = new HashMap<>();
        Map<String, Boolean> availableChannel = new HashMap<>();


        for(Map.Entry<String, Map<String, ChannelInfo>> mainChEntry : AppConfig.keyMainSubToChannelInfoByProtocol.get(protocol).entrySet()){

            for(Map.Entry<String, ChannelInfo> subChEntry: mainChEntry.getValue().entrySet()){
              ChannelInfo channelInfo = subChEntry.getValue();
              if(channelInfo.channelType == null){
                  availableChannel.put(channelInfo.key
                          +"*"+channelInfo._SensorName
                          +"*"+"["+mainChEntry.getKey()+"]"+channelInfo.name, Boolean.FALSE);
              }
          }



        }
        return (JSONObject) JSONObject.toJSON(availableChannel);
    }

    @RequestMapping(value = "/api/createStation",method = RequestMethod.POST)
    @ResponseBody
    private JSONObject createStationDo(@RequestBody String json)
   //     throws IOException
    {
        String sql, sqlDataTableString, sqlConfigTableString;
        try{
            JSONObject stationState=JSONObject.parseObject(json);
            String stationid = (String)stationState.get("stationid");
            String stationname= (String)stationState.get("stationname");
            String protocol = (String) stationState.get("protocol");
            stationState.remove("stationid");
            stationState.remove("stationname");
            stationState.remove("protocol");

            String measure = "";

            Map<String, WebConfig> stringWebConfigMap = new HashMap<>();

            sqlConfigTableString = "insert into `web_config` (`stationid`, `key`) values ";
            sqlDataTableString = "CREATE TABLE IF NOT EXISTS data_" + stationid
                    + " ( `id` BIGINT NOT NULL AUTO_INCREMENT " + ", `obtime` DATETIME PRIMARY KEY "
                    + ", `createtime` DATETIME DEFAULT CURRENT_TIMESTAMP " + ", `ps` VARCHAR DEFAULT NULL ";

            List<WebConfig> webConfigs = new LinkedList<>();
            Set<String> keySet= stationState.keySet();
            for (String key : keySet) {
                WebConfig webConfig = new WebConfig();
                webConfig.stationid = stationid;
                webConfig.key = key;
                stringWebConfigMap.put(key,webConfig);
                webConfigs.add(webConfig);

                measure += key+"-";
                sqlDataTableString += ", `" + key + "` VARCHAR DEFAULT NULL ";
               sqlConfigTableString += "( '"+stationid+"','"+key+"'),";
            }
            sql = "insert into `qx_station` (`stationid`, `alias`, `protocol`,`measure`) values ('"
                    +stationid+"','"
                    +stationname+"','"
                    +protocol+"','"
                    +measure
                    +"')";
            sqlDataTableString += ")";
            sqlConfigTableString = sqlConfigTableString.substring(0, sqlConfigTableString.length() - 1);

            //数据库手动事务操作
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("begin");
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sql);
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlDataTableString);
            DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlConfigTableString);
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("commit");
            StationState qxStation = new StationState();
            qxStation.stationid = stationid;
            qxStation.protocol = protocol;
            qxStation.measure = measure;
            qxStation.alias = stationname;

            StationInfo stationInfo = new StationInfo(new DataInfo(stationid),qxStation);
            stationInfo.keyToWebconfig = stringWebConfigMap;
            AppConfig.stationidTostationInfo.put(stationid,  stationInfo);
            Layui.initListCols();

        }catch (Exception ex){
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("rollback");
        }

        ChannelInfo sysUser=new ChannelInfo();
        //sysUser.setLoginPass("123456");
       // sysUser.setLoginAccount("小明");
      //  System.out.println("dskfjdsfssdfdsfklkdfdsf");
        JSONObject jsonObject= (JSONObject) JSONObject.toJSON(sysUser);
        Map<String,String>map=new HashMap<>();
        map.put("phone","1223456");
        map.put("status","ok");
        jsonObject.put("info",map);
        return jsonObject;
      //  response.setContentType("text/html;charset=utf-8");
       // response.getWriter().write(jsonObject.toJSONString());
    }
}
