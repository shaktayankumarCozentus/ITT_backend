package com.itt.service.fw.lit.service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface LitEngineService {
    String resolve(String litCode);
    String resolve(String litCode, Locale locale);
    Map<String, String> resolveBulk(List<String> litCodes, Locale locale);
    Map<String, String> resolveByPrefix(String prefix, Locale locale);
    ByteArrayInputStream exportAllMessagesAsExcel();
}
