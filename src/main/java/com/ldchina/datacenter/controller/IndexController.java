package com.ldchina.datacenter.controller;

import java.util.List;

import com.ldchina.datacenter.dao.entity.StationState;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.utils.DbUtil;
@Controller
public class IndexController {
    @RequestMapping("/")
    public ModelAndView index(Model model) {
    	List<StationState> stationStates = DbUtil.dbMapperUtil.qxStationMapper.getAllStations();
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("stations", stationStates);
        mav.addObject("webTitle",AppConfig.appConfig.webTitle);
        return mav;
    }

    @RequestMapping("login")
    public ModelAndView login(Boolean failed) {
    	ModelAndView mav = new ModelAndView("login");
       
        if (failed != null)
            mav.addObject("failed", 1);
        else {
        	mav.addObject("failed", 0);
        }
        return mav;
    }
    @RequestMapping("/detail")
    public ModelAndView detial(String stationid){
        ModelAndView mav = new ModelAndView("x");
        mav.addObject("stationState", AppConfig.stationidTostationInfo.get(stationid).stationState);
        return mav;
    }
}
