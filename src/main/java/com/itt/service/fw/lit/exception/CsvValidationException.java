package com.itt.service.fw.lit.exception;

import java.io.File;

public class CsvValidationException extends Exception {
    private final File errorFile;

    public CsvValidationException(File errorFile) {
        super("CSV validation failed");
        this.errorFile = errorFile;
    }

    public File getErrorFile() {
        return errorFile;
    }
}
