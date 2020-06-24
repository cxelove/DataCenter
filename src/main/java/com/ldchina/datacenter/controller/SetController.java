package com.ldchina.datacenter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.types.Layui;
import com.ldchina.datacenter.types.ZTree;
import com.ldchina.datacenter.types.Sensor;
import com.ldchina.datacenter.utils.DbUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/set")
public class SetController {

	private String getSetJsonStr(List<String> activeList) {
		List<Map<String, Object>> lst = DbUtil.dbMapperUtil.iSqlMapper
				.sqlget("SELECT `VALUE` FROM `serv_config` WHERE `property` = 'serv_current_sensor'");
		List<ZTree> zTrees = new ArrayList<ZTree>();
//		String typeString = "";
//		if (lst.get(0) != null) {
//			typeString =  (String)lst.get(0).get("VALUE");
//		}
//		char[] tmpchs = typeString.toCharArray();
//		for (int i = 0; i < tmpchs.length; i++) {
//			Character character = Character.valueOf(tmpchs[i]);
//			Sensor sensor = AppConfig.sensorMap.get(character);
//			if (sensor == null)
//				continue;
//			ZTree ztree = new ZTree();
//			ztree.name = sensor.name + "[ " + character.toString() + " ]";
//			ztree.children = new ArrayList<ZTree>();
//			Measurement[] measurements = sensor.measurement.get(character);
//			for (Measurement measurement : measurements) {
//				ZTree children = new ZTree();
//				children.name = measurement.name;
//				children.alias = measurement.key;
//				if (activeList.contains(children.alias)) {
//					children.checked = true;
//				} else {
//					children.checked = false;
//				}
//				ztree.children.add(children);
//			}
//			zTrees.add(ztree);
//		}




//    	for(Sensor sensor:AppConfig.sensorList){
//    		ZTree ztree = new ZTree();
//    		ztree.name = sensor.name;
//    		ztree.children = new ArrayList<ZTree>(); 
//    		
//    		for(Entry<Character, Measurement[]> entry:sensor.measurement.entrySet()) {   			
//    			ZTree ch2 = new ZTree();
//    			ch2.name = entry.getKey().toString();
//    			Measurement[] measurements = entry.getValue(); 
//    			
//    			if(typeString.indexOf(entry.getKey().charValue())<0) {
//    				ztree.checked=false;
//    				ztree.chkDisabled = true;
//    				ch2.chkDisabled = true;
//    				ch2.checked=false; 
//    				ch2.children=new ArrayList<ZTree>();
//    				for(int i=0;i<measurements.length;i++) {
//        				ZTree ltrTree = new ZTree();
//        				ltrTree.chkDisabled = true;
//        				//ltrTree.checked = false;
//        				ltrTree.name = measurements[i].name;
//        				ltrTree.alias = measurements[i].key;
//        				ch2.children.add(ltrTree);
//        			}
//    			}else {
//    				//ztree.checked=true;
//    				ztree.chkDisabled = false;
//    				
//    				ch2.chkDisabled = false;
//    				//ch2.checked=true; 
//    				ch2.children=new ArrayList<ZTree>();
//    				for(int i=0;i<measurements.length;i++) {
//        				ZTree ltrTree = new ZTree();
//        				ltrTree.chkDisabled = false;
//        				ltrTree.name = measurements[i].name;
//        				ltrTree.alias = measurements[i].key;
//        				if(activeList.contains(ltrTree.alias)) {
//        					ltrTree.checked = true;
//        				}else {
//        					ltrTree.checked = false;
//        				}        				
//        				ch2.children.add(ltrTree);
//        			}
//    			}
//    			ztree.children.add(ch2);
//    		}
//    		layuiTrees.add(ztree) ;    		
//    	};
		return JSONObject.toJSONString(zTrees);
//    	for(Map.Entry<Character, Sensor> entry: AppConfig.sensorMap.entrySet()) {
//    		LayuiTree layuiTree = new LayuiTree();
//    		if(types.indexOf((entry.getKey().charValue()))<0) {
//    			layuiTree.title = entry.getValue().name+"[ "+entry.getKey()+" ]";
//    			layuiTree.disabled = true;
//    			
//    		}else {
//    			
//    		}
//    	}
		// return null;

//        StringBuilder stringBuilder = new StringBuilder(3*1024);
//        stringBuilder.append("[");
		// 遍历map中的值
//        for (int i = 0; i < appConfig.webConfigMapKeyList.size(); i++) {
//            WebConfig webConfig = appConfig.webConfigMap.get(appConfig.webConfigMapKeyList.get(i));
//            stringBuilder.append("{ title:'")
//                    .append(appConfig.getSensorCommitByName(webConfig.getId().substring(1, 2)))
//                    .append(" [ ")
//                    .append(webConfig.getId().substring(1, 2))
//                    .append(" ]',id:'")
//                    .append(appConfig.getSensorCommitByName(webConfig.getId().substring(1, 2)))
//                    .append("',children:[{");
//            for (; ; i++) {
//                webConfig = appConfig.webConfigMap.get(appConfig.webConfigMapKeyList.get(i));
//                stringBuilder.append("title:'")
//                        .append(webConfig.getRemark())
//                        .append("',id:'")
//                        .append(webConfig.getId())
//                        .append("',");
//                switch (type) {
//                    case "map":
//                        if (!webConfig.getListDisplay()) {
//                            stringBuilder.append("disabled:true,");
//                        }
//                        stringBuilder.append("checked:")
//                                .append(webConfig.getMapDisplay())
//                                .append("}");
//                        break;
//                    case "list":
//                        stringBuilder.append("checked:")
//                                .append(webConfig.getListDisplay())
//                                .append("}");
//                        break;
//                    default:
//                        return "";
//                }
//                try {
//                    if (appConfig.webConfigMapKeyList.get(i + 1).charAt(1) != webConfig.getId().charAt(1)) {
//                        stringBuilder.append("]");
//                        break;
//                    }
//                } catch (IndexOutOfBoundsException e) {
//                    stringBuilder.append("]");
//                    break;
//                }
//                stringBuilder.append(",{");
//            }
//            stringBuilder.append("},");
//        }
//        stringBuilder.setCharAt(stringBuilder.length()-1,']');
//        return stringBuilder.toString();
		// return s.substring(0, s.length() - 1) + "]";
	}

	@RequestMapping("")
	public ModelAndView set() {
		ModelAndView mav = new ModelAndView("set");
		List<String> list = new ArrayList<String>();
		List<String> map = new ArrayList<String>();
//		for (WebConfig webConfig : AppConfig.webconfigs) {
//			if (webConfig.listDisplay)
//				list.add(webConfig.name);
//			if (webConfig.listDisplay && webConfig.mapDisplay)
//				map.add(webConfig.name);
//		}
		mav.addObject("list", getSetJsonStr(list));
		mav.addObject("map", getSetJsonStr(map));
		return mav;
	}

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public void save(HttpServletResponse response, @RequestBody String param, String type) {
		Map<String, Integer> map = new HashMap<>();
		try {
			List<ZTree> maps = JSONObject.parseArray(param, ZTree.class);
			String sqlString = "";
			for (ZTree layuiTree : maps) {
				{
					for (ZTree children : layuiTree.children) {
						//web_configs
						if ("list".equals(type)) {
							sqlString = "MERGE INTO `web_config` (`name`,`listdisplay`) VALUES('" + children.alias
									+ "'," + children.checked + ")";
						} else {
							sqlString = "MERGE INTO `web_config` (`name`,`mapdisplay`) VALUES('" + children.alias + "',"
									+ children.checked + ")";
						}
						DbUtil.dbMapperUtil.iSqlMapper.sqlput(sqlString);
					}
				}
			}
//			AppConfig.webconfigs = DbUtil.dbMapperUtil.webConfigMapper.selectAll();
//			Layui.initListCols(AppConfig.webconfigs);
			map.put("status", 0);
		} catch (

		Exception e) {
			map.put("status", -1);
		}
		try {
			response.getWriter().println(JSON.toJSONString(map));
		} catch (IOException e) {

		}
	}
//
//    List<WebConfig> Laytree2WebConfig(List<LayuiTree> layuiTrees) {
//        return null;
//    }
//
//    List<String> treeConfigs = new ArrayList<>();
//
//    void getConfig(List list) {
//        Iterator<LayuiTree> it1 = list.iterator();
//        while (it1.hasNext()) {
//            LayuiTree layuiTree = it1.next();
//            treeConfigs.add(layuiTree.id);
//            if (layuiTree.children != null) {
//                getConfig(layuiTree.children);
//            }
//        }
//    }
}
