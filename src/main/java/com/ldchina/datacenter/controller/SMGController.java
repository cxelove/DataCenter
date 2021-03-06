package com.ldchina.datacenter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.StationState;

import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.utils.DbUtil;

import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/smg")
public class SMGController {

    @RequestMapping("")
    public String smg() {
        return "smg";
    }

    @RequestMapping(value = "/edit")
    public ModelAndView edit(String stationId) {
        if (stationId == null) return null;
        ModelAndView mav = new ModelAndView("smg/edit");
        String protocol = AppConfig.stationidTostationInfo.get(stationId).stationState.protocol;
      //  List<Map<String, WebConfig>> retList = new ArrayList<>();
        Map<String, WebConfig> retMap = new HashMap<>();
        for (Map.Entry<String, WebConfig> webConfigEntry : AppConfig.stationidTostationInfo.get(stationId).keyToWebconfig.entrySet()) {
            String mainKey = webConfigEntry.getKey().split("_")[1];
            String subKey = webConfigEntry.getKey().split("_")[2];

            retMap.put(AppConfig.keyMainSubToChannelInfoByProtocol
                    .get(protocol)
                    .get(mainKey)
                    .get(subKey).name, webConfigEntry.getValue());
        }
        mav.addObject("mapList", retMap);
        mav.addObject("stationid", stationId);
        return mav;
    }

    @RequestMapping(value = "/cmd")
    public ModelAndView cmd(String stationId) {
        ModelAndView mav = new ModelAndView("smg/cmd");
        mav.addObject("stationid", stationId);
        return mav;
    }

    @RequestMapping(value = "/update")
    public ModelAndView update(String stationId, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("smg/update");
        String path = new ApplicationHome(getClass()).getSource().getParentFile().toString() + "\\upload\\bin";
        File file = new File(path);
        File[] fileList = file.listFiles();
        mav.addObject("stationid", stationId);
        String s = "";
        if (AppConfig.stationidTostationInfo
                .get(stationId).ioSession == null) {
            s = s + "[ 站点不在线 ] ";
            mav.addObject("ready", 0);
        } else {
            s = s + "[ 站点在线 ] ";
            if (fileList != null && fileList.length > 0) {
                s = s + "文件信息：" + fileList[0].getName() + " " + fileList[0].length() + "B";
                mav.addObject("ready", 1);
            } else {
                s = s + "请上传程序文件.";
                mav.addObject("ready", 2);
            }
        }
        mav.addObject("status", s);
        return mav;
    }

    @RequestMapping(value = "/del")
    public String delStation(String stationId) {
        if (!(stationId == null || stationId == "" || stationId == "undefined")) {
            //ReceiveThread.stationStatusMap.remove(stationId);
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("DROP TABLE IF EXISTS DATA_" + stationId);
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("DELETE FROM `qx_reupload` WHERE `stationid`='" + stationId + "'");
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("DELETE FROM `qx_latest` WHERE `stationid`='" + stationId + "'");
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("DELETE FROM `qx_station` WHERE `stationid`='" + stationId + "'");
            DbUtil.dbMapperUtil.iSqlMapper.sqlput("DELETE FROM `web_config` WHERE `stationid`='" + stationId + "'");
            AppConfig.stationidTostationInfo.remove(stationId);
        }
        return "smg";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> upload(MultipartFile file,
                                      HttpServletRequest request) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            File f = new File(new ApplicationHome(getClass()).getSource().getParentFile().toString() + "\\upload\\bin");
            if (!f.exists()) f.mkdirs();
            DeleteAll(f);
            int bin = uploadFile(file, f.toString());
            map.put("code", 0);
        } catch (Exception e) {
            map.put("code", 1);
            e.printStackTrace();
        }
        return map;
    }



    @Resource
    protected HttpServletRequest request;

    /**
     * 设置更新站点信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updatestationState", method = RequestMethod.POST)
    public String updatestationState() throws Exception {
        StringBuffer jb = new StringBuffer();
        String line = null;
        //try {
        BufferedReader reader = request.getReader();
        while ((line = reader.readLine()) != null)
            jb.append(line);
        JSONObject jsonObject = JSONObject.parseObject(jb.toString());
        StationState stationState = JSON.toJavaObject(jsonObject, StationState.class);
        DbUtil.dbMapperUtil.qxStationMapper.updatestationStateById(stationState);
        AppConfig.stationidTostationInfo.get(stationState.stationid).stationState = stationState;
        return "success";
        //   } catch (Exception ex) { /*report an error*/ }

        //  return "fail";

    }

    public static int uploadFile(MultipartFile file, String path)
            throws IOException {

        String name = file.getOriginalFilename();
        File tempFile = new File(path, name);
        if (!tempFile.getParentFile().exists()) {
            tempFile.getParentFile().mkdir();
        }
        tempFile.createNewFile();
        file.transferTo(tempFile);

        return 0;
    }

    private static void DeleteAll(File dir) {
        if (dir.isFile()) {
            System.out.println(dir + " : " + dir.delete());
            return;
        } else {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    DeleteAll(file);
                }
            }
        }
        // System.out.println(dir + " : " + dir.delete());
    }
}
