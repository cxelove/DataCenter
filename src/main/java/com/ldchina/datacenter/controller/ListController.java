package com.ldchina.datacenter.controller;

import com.ldchina.datacenter.types.StationStatus;
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

        List<Map.Entry<String, StationStatus>> list = new ArrayList<Map.Entry<String, StationStatus>>(AppConfig.stationidTostationStatus.entrySet());

        Collections.sort(list,new Comparator<Map.Entry<String, StationStatus>>() {
            //升序排序
            public int compare(Map.Entry<String, StationStatus> o1, Map.Entry<String, StationStatus> o2) {
                return o1.getValue().stationInfo.protocol.compareTo(o2.getValue().stationInfo.protocol);
            }
        });
        for (Map.Entry<String, StationStatus> e: list) {
            if(ret.get(e.getValue().stationInfo.measure)==null){
                ret.put(e.getValue().stationInfo.measure, Layui.getListColsByStationid(e.getValue().stationInfo.STATIONID));
            }
        }
        return  ret;
    }
}