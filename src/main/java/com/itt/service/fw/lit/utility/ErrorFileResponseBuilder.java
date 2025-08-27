package com.itt.service.fw.lit.utility;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;

import java.io.File;
import java.io.IOException;

@UtilityClass
public class ErrorFileResponseBuilder {

    public ResponseEntity<InputStreamResource> build(File errorFile) {
        try {
            var resource = new InputStreamResource(new AutoDeletingFileInputStream(errorFile));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .headers(headers(errorFile.getName()))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private HttpHeaders headers(String filename) {
        var headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.add("X-Status", "error");
        headers.add("X-Message", "CSV/Excel contains validation errors. Download the report.");
        return headers;
    }
}
