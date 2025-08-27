package com.itt.service.fw.lit.controller;

import com.itt.service.fw.audit.annotation.EventAuditLogger;
import com.itt.service.fw.lit.exception.CsvValidationException;
import com.itt.service.fw.lit.service.LitEngineService;
import com.itt.service.fw.lit.service.impl.LitCatalogService;
import com.itt.service.fw.lit.utility.ErrorFileResponseBuilder;
import com.itt.service.fw.lit.utility.LitCsvLoader;
import com.itt.service.fw.logger.api.annotation.Loggable;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/public/lit")
@RequiredArgsConstructor
public class LitController {

    private final LitEngineService litEngineService;

    private final LitCatalogService catalogService;

    private final LitCsvLoader litCsvLoader;

    @Operation(summary = "Fetch all LIT messages (all languages)")
    @GetMapping("/all")
    @Loggable
    public ResponseEntity<Map<String, Map<String, String>> > getAll() {
        return ResponseEntity.ok(catalogService.getAllMessagesGroupedByLanguage());
    }

    @Operation(summary = "Fetch all LIT messages for a specific language")
    @GetMapping("/all/{lang}")
    @Loggable
    public ResponseEntity<Map<String, String>> getAllByLanguage(@PathVariable String lang) {
        return ResponseEntity.ok(catalogService.getMessagesForLanguage(lang));
    }

    @Operation(summary = "Single message resolution", description = "API for Single message resolution")
    @GetMapping("/resolve")
    @Loggable
    public ResponseEntity<String> getMessage(@RequestParam String code, @RequestParam(required = false) String lang) {
        Locale locale = (lang != null) ? Locale.forLanguageTag(lang) : LocaleContextHolder.getLocale();
        return ResponseEntity.ok(litEngineService.resolve(code, locale));
    }

    @Operation(summary = "Bulk message fetch", description = "API for Bulk message fetch")
    @PostMapping("/resolve/bulk")
    @Loggable
    public ResponseEntity<Map<String, String>> getBulk(@RequestBody (required = false) List<String> codes,
                                                       @RequestParam(required = false) String lang) {
        Locale locale = (lang != null) ? Locale.forLanguageTag(lang) : LocaleContextHolder.getLocale();
        return ResponseEntity.ok(litEngineService.resolveBulk(codes, locale));
    }

    @Operation(summary = "Prefix-based screen-level fetch", description = "API for Prefix-based screen-level fetch")
    @GetMapping("/prefix")
    @Loggable
    public ResponseEntity<Map<String, String>> getMessagesByPrefix(@RequestParam String prefix,
                                                                   @RequestParam(required = false) String lang) {
        Locale locale = (lang != null) ? Locale.forLanguageTag(lang) : LocaleContextHolder.getLocale();
        return ResponseEntity.ok(litEngineService.resolveByPrefix(prefix, locale));
    }

    @Operation(summary = "CSV-based LIT message loading", description = "API for CSV-based LIT message loading")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Loggable//(propagate = true)
    public ResponseEntity<?> uploadLITMessages(@RequestParam("file") MultipartFile file,@RequestParam(required = false) String lang) {
        final Locale locale = (lang != null && !lang.isBlank())
                ? Locale.forLanguageTag(lang)
                : LocaleContextHolder.getLocale();
        try (InputStream inputStream = file.getInputStream()) {
            litCsvLoader.load(inputStream, file.getOriginalFilename(), locale);
            return ResponseEntity.ok("LIT messages uploaded successfully.");
        } catch (CsvValidationException ex) {
            return ErrorFileResponseBuilder.build(ex.getErrorFile());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred: " + ex.getMessage());
        }
    }




//    @Operation(summary = "CSV-based LIT message loading", description = "API for CSV-based LIT message loading")
//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> uploadLITMessages(@RequestParam("file") MultipartFile file) {
//        try {
//            litCsvLoader.load(file.getInputStream(), file.getOriginalFilename());
//            return ResponseEntity.ok("LIT messages uploaded successfully.");
//        } catch (CsvValidationException ex) {
//            String downloadPath = "/api/lit/upload/error-file/" + ex.getErrorFile().getName();
//            Map<String, Object> response = Map.of(
//                    "status", "error",
//                    "message", "CSV contains errors. Download the report.",
//                    "errorReport", downloadPath
//            );
//            return ResponseEntity.badRequest().body(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("status", "failure", "message", e.getMessage()));
//        }
//    }

    @GetMapping("/upload/error-file/{filename}")
    @Loggable
    public ResponseEntity<Resource> downloadErrorFile(@PathVariable String filename) {
        File file = new File(System.getProperty("java.io.tmpdir"), filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            InputStreamResource resource = new InputStreamResource(fis);

            ResponseEntity<Resource> response = ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

            // Delete file after serving
            file.delete();

            return response;
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Operation(summary = "Download all LIT messages", description = "Exports LIT_MESSAGE data to Excel")
    @GetMapping("/download-all")
    @Loggable
    public ResponseEntity<?> downloadAllMessages() {
        try {
            var stream = litEngineService.exportAllMessagesAsExcel();
            var resource = new InputStreamResource(stream);

            String filename = "lit-messages-%s.xlsx".formatted(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate Excel: " + e.getMessage());
        }
    }

}
