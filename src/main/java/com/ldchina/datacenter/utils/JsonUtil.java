package com.ldchina.datacenter.utils;

import com.alibaba.fastjson.JSON;

import java.io.*;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    /**
     * 遍历文件夹下所有文件，返回路径
     */
    private static List<String> getAllFileInDir(String path) {
        List<String> stringList = new ArrayList<String>();
        File file = new File(path);        //获取其file对象
        File[] fs = file.listFiles();    //遍历path下的文件和目录，放在File数组中
        for (File f : fs) {                    //遍历File[]数组
            if (!f.isDirectory())        //若非目录(即文件)，则打印
                stringList.add(f.getPath());
        }
        return stringList;
    }

    /**
     * 读取json文件，返回json串
     *
     * @param fileName
     * @return
     */
    public static String readJsonFile(String fileName) {
            StringBuilder result = new StringBuilder();
            try{
                FileInputStream fis = new FileInputStream(fileName);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    result.append(line);
                    result.append("\r\n"); // 补上换行符   
                }
                br.close();
                isr.close();
                fis.close();
                return result.toString();

            }catch(Exception e){
                e.printStackTrace();
                return "{}";
            }

    }
    public static<T> List<T> getAllJavaObjInDir(String path, Class<T> clazz){
        List<T> list = new ArrayList<T>();
        List<String> stringList = getAllFileInDir(path);
        for(int i=0;i<stringList.size();i++){
            try{
                list.add(JSON.parseObject(readJsonFile(stringList.get(i)),clazz));
            }catch (Exception ex){
                System.out.println("读取传感器配置出错："+stringList.get(i));
                ex.printStackTrace();
            }
        }
        return list;
    }
    /**
     * @Author：
     * @Description：获取某个目录下所有直接下级文件，不包括目录下的子目录的下的文件，所以不用递归获取
     * @Date：
     */
    public static List<String> getFiles(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                files.add(tempList[i].toString());
                //文件名，不包含路径
                //String fileName = tempList[i].getName();
            }
        }
        return files;
    }
    /**
     * @Author：
     * @Description：获取某个目录下所有直接下级文件，不包括目录下的子目录的下的文件，所以不用递归获取
     * @Date：
     */
    public static List<String> getDirectories(String path) {
        List<String> files = new ArrayList<String>();
        File file = new File(path);
        File[] tempList = file.listFiles();

        for (int i = 0; i < tempList.length; i++) {
//            if (tempList[i].isFile()) {
//                files.add(tempList[i].toString());
//                //文件名，不包含路径
//                //String fileName = tempList[i].getName();
//            }
            if (tempList[i].isDirectory()) {
                //这里就不递归了，
                files.add(tempList[i].toString());
            }
        }
        return files;
    }
}
