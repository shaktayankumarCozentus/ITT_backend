package com.itt.service.validator;

import com.itt.service.constants.ErrorMessages;
import com.itt.service.dto.ApiResponse;
import com.itt.service.enums.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public class RequestValidator {

    public static <T> ApiResponse<?> validateRequiredFields(Map<String, T> fields) {
        for (Map.Entry<String, T> entry : fields.entrySet()) {
            T value = entry.getValue();
            if (value == null || (value instanceof String str && str.isBlank())
                    || (value instanceof MultipartFile file && file.isEmpty())) {
                return ApiResponse.error(
                        ErrorCode.MISSING_REQUIRED_FIELD,
                        ErrorMessages.MISSING_REQUIRED_FIELD.formatted(entry.getKey())
                );
            }
        }
        return null; // means all valid
    }
}

