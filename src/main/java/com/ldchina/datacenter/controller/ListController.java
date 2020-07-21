package com.ldchina.datacenter.controller;

import com.ldchina.datacenter.types.StationInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.types.Layui;

import java.util.*;

@Controller
public class ListController {
    @RequestMapping("/list")
    public ModelAndView list() {
        ModelAndView mav = new ModelAndView("list");

        return mav;
    }
    @RequestMapping("/api/getListTable")
    @ResponseBody
    public Map getListTable(){
        Map<String, String> ret = new LinkedHashMap<>();

        List<Map.Entry<String, StationInfo>> list = new ArrayList<Map.Entry<String, StationInfo>>(AppConfig.stationidTostationInfo.entrySet());

        Collections.sort(list,new Comparator<Map.Entry<String, StationInfo>>() {
            //升序排序
            public int compare(Map.Entry<String, StationInfo> o1, Map.Entry<String, StationInfo> o2) {
                return o1.getValue().stationState.protocol.compareTo(o2.getValue().stationState.protocol);
            }
        });
        for (Map.Entry<String, StationInfo> e: list) {
            if(ret.get(e.getValue().stationState.measure)==null){
                ret.put(e.getValue().stationState.measure, Layui.getListColsByStationid(e.getValue().stationState.stationid));
            }
        }
        return  ret;
    }
}