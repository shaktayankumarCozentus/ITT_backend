package com.itt.service.util;

import org.springframework.stereotype.Component;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * Resolves parameterized messages for error and success responses.
 * 
 * Examples:
 * - "User {0} not found" + args["john"] = "User john not found"
 * - "Updated {0} records" + args[5] = "Updated 5 records"
 */
@Component
public class MessageResolver {
    
    /** Replaces {0}, {1}, etc. placeholders with provided arguments */
    public String resolveMessage(String messageTemplate, Object... args) {
        if (args == null || args.length == 0) {
            return messageTemplate;
        }
        return MessageFormat.format(messageTemplate, args);
    }
    
    /** Future support for multiple languages - currently uses default locale */
    public String resolveMessage(String messageTemplate, Locale locale, Object... args) {
        return resolveMessage(messageTemplate, args);
    }
}