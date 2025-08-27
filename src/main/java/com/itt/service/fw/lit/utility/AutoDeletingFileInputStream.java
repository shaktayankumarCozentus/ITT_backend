package com.itt.service.fw.lit.utility;

import java.io.*;

public class AutoDeletingFileInputStream extends FileInputStream {
    private final File file;

    public AutoDeletingFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    @Override
    public void close() throws IOException {
        super.close();
        if (file.exists() && !file.delete()) {
            System.err.println("Warning: Failed to delete temp file: " + file.getAbsolutePath());
        }
    }
}


