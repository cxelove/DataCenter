package com.ldchina.datacenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/report")
public class ReportController {
    @RequestMapping("")
    public ModelAndView report(){
        ModelAndView mav = new ModelAndView("report");
        return mav;
    }
}
