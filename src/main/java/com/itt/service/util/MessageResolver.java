package com.itt.service.util;


import org.springframework.stereotype.Component;
import java.text.MessageFormat;
import java.util.Locale;

@Component
public class MessageResolver {
    
    public String resolveMessage(String messageTemplate, Object... args) {
        if (args == null || args.length == 0) {
            return messageTemplate;
        }
        return MessageFormat.format(messageTemplate, args);
    }
    
    public String resolveMessage(String messageTemplate, Locale locale, Object... args) {
        // Future enhancement: Support for internationalization
        return resolveMessage(messageTemplate, args);
    }
}