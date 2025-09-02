package com.itt.service.constants;

import java.util.Arrays;
import java.util.List;

public final class DocumentRelatedConstants {
	
	public static final List<String> PERMITED_FILE_TYPES_PDF = Arrays.asList(".pdf","text/pdf", "application/pdf", "text/x-csv", "application/x-pdf", "text/comma-separated-values", "text/x-comma-separated-values");
	public static final double BYTES_PER_MB = 1_000_000d; // decimal MB
	public static final double MAX_FILE_SIZE_MB = 5d;
	public static final String RELEASE_NOTES_PREFIX = "Release_Notes";
}
