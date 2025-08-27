package com.itt.service.fw.lit.utility;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LitExcelExporter {

//    public ByteArrayInputStream toExcel(List<LitMessage> messages) throws IOException {
//        try (Workbook workbook = new XSSFWorkbook()) {
//            Sheet sheet = workbook.createSheet("LIT Messages");
//
//            // Header
//            Row header = sheet.createRow(0);
//            header.createCell(0).setCellValue("LIT_CODE");
//            header.createCell(1).setCellValue("LANGUAGE_CODE");
//            header.createCell(2).setCellValue("MESSAGE");
//
//            // Body
//            for (int i = 0; i < messages.size(); i++) {
//                LitMessage msg = messages.get(i);
//                Row row = sheet.createRow(i + 1);
//                row.createCell(0).setCellValue(msg.getId().getLitCode());
//                row.createCell(1).setCellValue(msg.getId().getLanguageCode());
//                row.createCell(2).setCellValue(msg.getMessage());
//            }
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            workbook.write(out);
//            return new ByteArrayInputStream(out.toByteArray());
//        }
//    }
}
