package com.ldchina.datacenter.controller;

import com.ldchina.datacenter.AppConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping("/home")
public class HomeController {
    @RequestMapping("")
    public ModelAndView home() {
        ModelAndView mav = new ModelAndView("home");
//        List<Measurement> measurements = new ArrayList<Measurement>();
//        List<WebConfig> webConfigs = AppConfig.webconfigs;
//        AppConfig.stationidTostationStatus.get()
//        if(webConfigs!=null) {
//        	webConfigs.forEach(config->{
//        		if(config.mapDisplay)
//        			measurements.add(AppConfig.measurementMap.get(config.name));
//        	});
//        }
//        if(measurements.size() == 0) {
//        	mav.addObject("mapDisplay");
//        }else {
//        	mav.addObject("mapDisplay",JSONArray.toJSONString(measurements));
//        }
        return mav;
    }
}
