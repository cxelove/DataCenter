package com.ldchina.datacenter.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/summary")
public class SummaryController {
    @RequestMapping("")
    public ModelAndView Summary(){
        ModelAndView mav = new ModelAndView("summary");
        return mav;
    }
}
