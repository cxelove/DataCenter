package com.ldchina.datacenter.controller;

import com.ldchina.datacenter.AppConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/export")
public class ExportController {

    @RequestMapping("")
    public ModelAndView export() {
        ModelAndView mav = new ModelAndView("export");
       //Map

       // mav.addObject("cols", com.ldchina.datacenter.types.Layui.getListCols(AppConfig.webconfigs));
        return mav;
    }

//    // 自适应宽度(中文支持)
//    private void setSizeColumn(XSSFSheet sheet, int size) {
//        for (int columnNum = 0; columnNum < size; columnNum++) {
//            int columnWidth = sheet.getColumnWidth(columnNum) / 256;
//            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
//                XSSFRow currentRow;
//                //当前行未被使用过
//                if (sheet.getRow(rowNum) == null) {
//                    currentRow = sheet.createRow(rowNum);
//                } else {
//                    currentRow = sheet.getRow(rowNum);
//                }
//
//                if (currentRow.getCell(columnNum) != null) {
//                    XSSFCell currentCell = currentRow.getCell(columnNum);
//                    if (currentCell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
//                        int length = currentCell.getStringCellValue().getBytes().length;
//                        if (columnWidth < length) {
//                            columnWidth = length;
//                        }
//                    }
//                }
//            }
//            sheet.setColumnWidth(columnNum, columnWidth * 256);
//        }
//    }

//    @RequestMapping("/download")
//    public ResponseEntity<byte[]> download(String stationId, String date) {
//        XSSFWorkbook workbook = new XSSFWorkbook();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        XSSFSheet sheet = workbook.createSheet();
//        QxData qxData = new QxData();
//        try {
//            qxData.setObtime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
//            qxData.setStationid(stationId);
//            List<QxData> qxDatas = dbCache.qxDataMapper.getOneday(qxData);
//            List<JsQxData> jsQxDatas = new ArrayList<>();
//            for (int i = 0; i < qxDatas.size(); i++) {
//                jsQxDatas.add(i, qxDatas.get(i).toJsData());
//            }
//            for (int i = 0; i < qxDatas.size() + 1; i++) {
//                sheet.createRow(i);
//            }
//            sheet.getRow(0).createCell(0).setCellValue("观测时间");
//            sheet.getRow(0).createCell(1).setCellValue("电压(v)");
//            sheet.autoSizeColumn((short) 0);
//            sheet.autoSizeColumn((short) 1);
//            for (int n = 0; n < jsQxDatas.size(); n++) {
//                sheet.getRow(n + 1).createCell(0).setCellValue(jsQxDatas.get(n).obtime);
//                sheet.getRow(n + 1).createCell(1).setCellValue(Float.parseFloat(jsQxDatas.get(n).ps));
//            }
//            int cell = 2;
//            for (int i = 0; i < appConfig.webConfigMapKeyList.size(); i++) {
//                WebConfig webConfig = appConfig.webConfigMap.get(appConfig.webConfigMapKeyList.get(i));
//                if (webConfig.getListDisplay()) {
//                    //设置表头
//                    if (webConfig.getUnit().equals("")) {
//                        sheet.getRow(0).createCell(cell).setCellValue(webConfig.getRemark());
//                    } else {
//                        sheet.getRow(0).createCell(cell).setCellValue(webConfig.getRemark() + "(" + webConfig.getUnit() + ")");
//                    }
//                    sheet.autoSizeColumn((short) cell);
//                    //设置一列数据
//                    for (int n = 0; n < jsQxDatas.size(); n++) {
//                        Field field = JsQxData.class.getDeclaredField(webConfig.getId().toLowerCase());
//                        //设置对象的访问权限，保证对private的属性的访问
//                        field.setAccessible(true);
//                        String val = (String) field.get(jsQxDatas.get(n));
//
//                        try {
//                            sheet.getRow(n + 1).createCell(cell).setCellValue(Double.parseDouble(val));
//                        } catch (Exception e) {
//                            sheet.getRow(n + 1).createCell(cell).setCellValue(val);
//                        }
//                    }
//                    cell++;
//                }
//            }
//            setSizeColumn(sheet, cell - 1);
//            workbook.write(baos);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String filename = stationId + "_" +dbCache.qxStationMapper.getStationInfoById(stationId).getAlias()+"_"+ date + ".xls";
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-Disposition", "attachment;filename=" + filename);
//        return new ResponseEntity<byte[]>(baos.toByteArray(), headers, HttpStatus.CREATED);
//    }
}
