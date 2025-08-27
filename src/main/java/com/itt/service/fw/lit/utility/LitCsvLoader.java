package com.itt.service.fw.lit.utility;

import com.itt.service.entity.LitMessage;
import com.itt.service.entity.LitMessageId;
import com.itt.service.fw.lit.dto.UploadStats;
import com.itt.service.fw.lit.enums.LitMessageCode;
import com.itt.service.fw.lit.enums.UploadStatus;
import com.itt.service.fw.lit.exception.CsvValidationException;
import com.itt.service.fw.lit.service.impl.LitAuditService;
import com.itt.service.fw.logger.api.annotation.Loggable;
import com.itt.service.repository.LitMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
@RequiredArgsConstructor
@Loggable
public class LitCsvLoader {

    private final LitMessageRepository messageRepository;
    private final LitAuditService auditService;
    private final CommonUtils commonUtils;

    @Transactional(rollbackFor = Exception.class)
    @Loggable
    public File load(InputStream input, String originalFilename, Locale locale) throws Exception {
        List<String[]> errorRows = new ArrayList<>();
        List<LitMessage> upserts = new ArrayList<>();
        UploadStats stats = new UploadStats();

        if (originalFilename.endsWith(".csv")) {
            try (var reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                var lines = reader.lines().toList();
                for (int i = 1; i < lines.size(); i++) {
                    processRow(lines.get(i), i + 1, errorRows, upserts, stats, locale);
                }
            }
        } else if (originalFilename.endsWith(".xlsx") || originalFilename.endsWith(".xls")) {
//            try (Workbook workbook = WorkbookFactory.create(input)) {
//                Sheet sheet = workbook.getSheetAt(0);
//                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
//                    Row row = sheet.getRow(i);
//                    if (row == null) continue;
//                    String csv = Stream.of(getCellValue(row.getCell(0)), getCellValue(row.getCell(1)), getCellValue(row.getCell(2)))
//                            .collect(Collectors.joining(","));
//                    processRow(csv, i + 1, errorRows, upserts, stats,locale);
//                }
//            }
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + originalFilename);
        }

        if (!errorRows.isEmpty()) {
            stats.errorCount = errorRows.size();
            auditService.saveAudit(originalFilename, UploadStatus.FAILED, stats);
            throw new CsvValidationException(createErrorExcel(errorRows, originalFilename));
        }

        messageRepository.saveAll(upserts);
        auditService.saveAudit(originalFilename, UploadStatus.SUCCESS, stats);
        return null;
    }

//    private String getCellValue(Cell cell) {
//        if (cell == null) return "";
//        return switch (cell.getCellType()) {
//            case STRING -> cell.getStringCellValue();
//            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
//            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
//            case FORMULA -> cell.getCellFormula();
//            default -> "";
//        };
//    }

    private void processRow(String line, int rowNum, List<String[]> errorRows, List<LitMessage> upserts, UploadStats stats, Locale locale) {
        stats.totalProcessed++;

        if (line == null || line.trim().isEmpty()) {
            stats.errorCount++;
            addError(errorRows, rowNum, LitMessageCode.EMPTY_LINE, locale);
            return;
        }

        String[] parts = line.split(",", -1);
        if (parts.length < 3 || Arrays.stream(parts).limit(3).anyMatch(String::isBlank)) {
            stats.errorCount++;
            addError(errorRows, rowNum, LitMessageCode.INCOMPLETE_FIELDS, locale);
            return;
        }

        String litCode = parts[0].trim();
        String langCode = parts[1].trim();
        String message = parts[2].trim();
        LitMessageId id = new LitMessageId(litCode, langCode);

        LitMessage entity = messageRepository.findById(id)
                .map(existing -> {
                    existing.setMessage(message);
                    stats.updated++;
                    return existing;
                })
                .orElseGet(() -> {
                    stats.inserted++;
                    return new LitMessage(id, message);
                });

        upserts.add(entity);
    }
    private void addError(List<String[]> errors, int row, LitMessageCode code, Locale locale) {
        String message = commonUtils.i18n(code.getMessageKey(), locale);
        log.warn("Row {}: {}", row, message);
        errors.add(new String[]{String.valueOf(row), code.getCode(), message});
    }

    private File createErrorExcel(List<String[]> errorRows, String originalFilename) throws IOException {
        var currentTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String timestamp = currentTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace(":", "-");
        String filename = "lit-upload-errors-%s.xlsx".formatted(timestamp);
        File file = new File(System.getProperty("java.io.tmpdir"), filename);

//        try (Workbook workbook = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(file)) {
//            Sheet sheet = workbook.createSheet("Errors");
//
//            // Metadata
//            sheet.createRow(0).createCell(0).setCellValue("Filename:");
//            sheet.getRow(0).createCell(1).setCellValue(originalFilename);
//            sheet.createRow(1).createCell(0).setCellValue("Generated at:");
//            sheet.getRow(1).createCell(1).setCellValue(timestamp);
//            sheet.createRow(2); // blank row
//
//            // Header
//            Row header = sheet.createRow(3);
//            header.createCell(0).setCellValue("Row No");
//            header.createCell(1).setCellValue("Error Code");
//            header.createCell(2).setCellValue("Error Message");
//
//            // Body
//            for (int i = 0; i < errorRows.size(); i++) {
//                Row row = sheet.createRow(i + 4);
//                String[] err = errorRows.get(i);
//                row.createCell(0).setCellValue(err[0]); // Row Number
//                row.createCell(1).setCellValue(err[1]); // Error Code
//                row.createCell(2).setCellValue(err[2]); // Localized Message
//            }
//
//            workbook.write(out);
//        }

        return file;
    }
}
