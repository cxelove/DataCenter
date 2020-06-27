package com.ldchina.datacenter.utils;

import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

public class TxtUtil {
	private final static Logger log = LoggerFactory.getLogger(TxtUtil.class);
    /**
     * 获取文件编码类型
     *
     * @param bytes 文件bytes数组
     * @return      编码类型
     */
    public static String getEncoding(byte[] bytes) {
        String defaultEncoding = "UTF-8";
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(bytes, 0, bytes.length);
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        detector.reset();
        log.info("字符编码是：{}", encoding);
        if (encoding == null) {
            encoding = defaultEncoding;
        }
        return encoding;
    }

	/**
	 * 存储原始报文在时间指定的行
	 * @param msg
	 * @return
	 */
    public boolean StoreRecord(String msg) {
        try {
            String obTimeString =msg.split(",")[2];

            String path = GetPath() + msg.substring(1, 4) + "\\";
            File tmp = new File(path);
            if (!tmp.exists())
                tmp.mkdir();            
            path += obTimeString.substring(0, 8) + ".txt";
            StoreRecordByLine(path, msg, GetNumberByTime(TimeUtil.parseDate(obTimeString, "yyyyMMddHHmm")));
            return true;
        } catch (Exception ex) {
        	log.error("STORE DATA ERROR:",ex);
            return false;
        }

    }

    private int GetNumberByTime(Date dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateTime);
        return calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE) + 1;
    }

    private int StoreRecordByLine(String fileName, String newStr, int line) throws IOException {
        if (fileName == null) return -1;
        String temp = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileOutputStream fos = null;
        PrintWriter pw = null;

        File tmpfile = new File(fileName + ".tmp");
        tmpfile.createNewFile();

        try {

            //  tmpfile.

            StringBuffer sbu1 = new StringBuffer();

            if (new File(fileName).exists()) {
                fis = new FileInputStream(fileName);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);

                StringBuffer sbu2 = new StringBuffer();
                // 保存该文件原有的内容
                int j = 1;
                for (; (temp = br.readLine()) != null; ) {
                    if (j < line) {
                        sbu1 = sbu1.append(temp).append(LineDelimiter.WINDOWS.getValue());
                    } else if (j > line) {
                        sbu2 = sbu2.append(temp).append(LineDelimiter.WINDOWS.getValue());
                    }
                    j++;
                }
                if (sbu2.length() > 2) sbu2.delete(sbu2.length() - 2, sbu2.length());
                j = j - 1;
                if (j < line) {
                    for (int i = 1; i < line - j; i++) {
                        sbu1.append(LineDelimiter.WINDOWS.getValue());
                    }
                    sbu1.append(newStr);
                } else if (j > line) {
                    // 将新的内容追加到读取文件中的内容字符之后
                    sbu1.append(newStr).append(LineDelimiter.WINDOWS.getValue()).append(sbu2.toString());
                } else {
                    sbu1.append(LineDelimiter.WINDOWS.getValue()).append(sbu2.toString());
                }
            } else {
                int j = 1;
                while (j++ < line) {
                    sbu1.append(LineDelimiter.WINDOWS.getValue());
                }
                sbu1.append(newStr);
            }


            fos = new FileOutputStream(tmpfile);
            pw = new PrintWriter(fos);
            pw.write(sbu1.toString().toCharArray());
            pw.flush();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.close();
            }
            if (fos != null) {
                fos.close();
            }
            if (br != null) {
                br.close();
            }
            if (isr != null) {
                isr.close();
            }
            if (fis != null) {
                fis.close();
            }

        }
        new File(fileName).delete();
        tmpfile.renameTo(new File(fileName));
        return 0;
    }

    private String GetPath() {
        String path = new ApplicationHome(getClass()).getSource().getParentFile().toString() + "\\data\\";
        File file = new File(path);
        if (!file.exists())
            file.mkdir();
        return path;
    }
}
