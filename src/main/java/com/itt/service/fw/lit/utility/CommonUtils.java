package com.itt.service.fw.lit.utility;

import com.itt.service.entity.LitMessage;
import com.itt.service.entity.LitMessageId;
import com.itt.service.repository.LitMessageRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CommonUtils {

    private final LitMessageRepository messageRepository;

    public String i18n(String litCode, @Nullable Locale locale) {
        String langCode = (locale != null && StringUtils.hasText(locale.getLanguage()))
                ? locale.getLanguage()
                : "en";

        return messageRepository.findById(new LitMessageId(litCode, langCode))
                .map(LitMessage::getMessage)
                .orElse("!!" + litCode + "!!");
    }
}
