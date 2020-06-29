package com.ldchina.datacenter.controller;

import com.ldchina.datacenter.AppConfig;
import com.ldchina.datacenter.dao.entity.WebConfig;
import com.ldchina.datacenter.sensor.ChannelInfo;
import com.ldchina.datacenter.types.Layui;
import com.ldchina.datacenter.utils.DbUtil;
import com.ldchina.datacenter.utils.TimeUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/export")
public class ExportController {

    @RequestMapping("")
    public ModelAndView export(String stationid, String date) {
        ModelAndView mav = new ModelAndView("export");
        mav.addObject("cols", Layui.getListColsByStationid(stationid));
        mav.addObject("stationid", stationid);
        mav.addObject("date", TimeUtil.format(new Date(Long.parseLong(date)), "yyyy-MM-dd"));
        mav.addObject("alias",AppConfig.stationidTostationStatus.get(stationid).stationInfo.alias);

        // mav.addObject("cols", com.ldchina.datacenter.types.Layui.getListCols(AppConfig.webconfigs));
        return mav;
    }

    // 自适应宽度(中文支持)
    private void setSizeColumn(XSSFSheet sheet, int size) {
        for (int columnNum = 0; columnNum < size; columnNum++) {
            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                XSSFRow currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }

                if (currentRow.getCell(columnNum) != null) {
                    XSSFCell currentCell = currentRow.getCell(columnNum);
                    if (currentCell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            sheet.setColumnWidth(columnNum, columnWidth * 256);
        }
    }

    @RequestMapping("/download")
    public ResponseEntity<byte[]> download(String stationId, String date) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XSSFSheet sheet = workbook.createSheet();
        try {
            if (DbUtil.dbMapperUtil.iSqlMapper.isTableExit("DATA_" + stationId)) {
                String sqlString = "SELECT * FROM DATA_" + stationId + " WHERE DATEDIFF(day,obTime,'" + date
                        + "')=0 ORDER BY obTime DESC";
                List<Map<String, Object>> resultList = DbUtil.dbMapperUtil.iSqlMapper.sqlget(sqlString);
                for (int i = 0; i < resultList.size() + 1; i++) {
                    sheet.createRow(i);
                }
                sheet.getRow(0).createCell(0).setCellValue("观测时间");
                sheet.getRow(0).createCell(1).setCellValue("电压(V)");
                for (int n = 0; n < resultList.size(); n++) {
                    sheet.getRow(n + 1).createCell(0).setCellValue(TimeUtil.format((Date) resultList.get(n).get("OBTIME"), "yyyy-MM-dd HH:mm"));
                    sheet.autoSizeColumn((short) 0);
                    try{
                        sheet.getRow(n + 1).createCell(1).setCellValue(Double.parseDouble((String) resultList.get(n).get("PS")));
                    }catch (Exception ex){
                        sheet.getRow(n + 1).createCell(1).setCellValue((String) resultList.get(n).get("PS"));
                    }
                    sheet.autoSizeColumn((short) 1);
                }
                int cell = 2;
                for (Map.Entry<String, WebConfig> webConfigEntry : AppConfig.keyToWebconfigByStationid.get(stationId).entrySet()) {
                    String mainKey = webConfigEntry.getKey().split("_")[1];
                    String subKey = webConfigEntry.getKey().split("_")[2];
                    ChannelInfo channelInfo = AppConfig.keyMainSubToChannelInfoByProtocol
                            .get(AppConfig.stationidTostationStatus.get(stationId).stationInfo.protocol)
                            .get(mainKey)
                            .get(subKey);
                    if (channelInfo.unit == null) {
                        sheet.getRow(0).createCell(cell).setCellValue(channelInfo.name);
                    } else {
                        sheet.getRow(0).createCell(cell).setCellValue(channelInfo.name + "(" + channelInfo.unit + ")");
                    }
                    for (int n = 0; n < resultList.size(); n++) {
                        try {
                            sheet.getRow(n + 1).createCell(cell).setCellValue(Double.parseDouble((String) resultList.get(n).get(channelInfo.key)));
                        } catch (Exception e) {
                            sheet.getRow(n + 1).createCell(cell).setCellValue((String) resultList.get(n).get(channelInfo.key));
                        }
                    }
                    sheet.autoSizeColumn((short) cell);
                    setSizeColumn(sheet, cell);
                    cell++;
                }
                workbook.write(baos);
            } else {
                System.out.println("Data Table is NOT Exist.");
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
        String filename = stationId + "_" + AppConfig.stationidTostationStatus.get(stationId).stationInfo.alias + "_" + date + ".xls";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment;filename=" + filename);
        return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
    }
}
