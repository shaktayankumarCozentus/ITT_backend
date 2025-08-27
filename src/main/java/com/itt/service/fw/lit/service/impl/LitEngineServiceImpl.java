package com.itt.service.fw.lit.service.impl;

import com.itt.service.entity.LitMessage;
import com.itt.service.entity.LitMessageId;
import com.itt.service.fw.lit.exception.NoDataFoundException;
import com.itt.service.fw.lit.service.LitEngineService;
import com.itt.service.fw.lit.utility.CommonUtils;
import com.itt.service.fw.logger.api.annotation.Loggable;
import com.itt.service.repository.LitMessageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LitEngineServiceImpl implements LitEngineService {
    private final LitMessageRepository messageRepository;
    private final CommonUtils commonUtils;

    @Override
    @Loggable
    public String resolve(String litCode) {
        return resolve(litCode, LocaleContextHolder.getLocale());
    }

    @Override
    @Loggable
    public String resolve(String litCode, Locale locale) {
        return messageRepository.findById(LitMessageId.builder().litCode(litCode).languageCode(locale.getLanguage()).build())
                .map(LitMessage::getMessage)
                .orElse("??" + litCode + "??");
    }

    @Override
    @Loggable
    public Map<String, String> resolveBulk(List<String> litCodes, Locale locale) {
        return (litCodes == null || litCodes.isEmpty())
                ? getAllLitCodes(locale.getLanguage())
                : getByLitCodes(litCodes, locale.getLanguage());
    }

    private Map<String, String> getByLitCodes(List<String> litCodes, String language) {
        return messageRepository.findAllByLitCodes(litCodes, language)
                .stream().collect(Collectors.toMap(msg -> msg.getId().getLitCode(), LitMessage::getMessage));
    }

    private Map<String, String> getAllLitCodes(String language) {
        return messageRepository.findByIdLanguageCode(language)
                .stream().collect(Collectors.toMap(msg -> msg.getId().getLitCode(), LitMessage::getMessage));
    }

    @Override
    @Loggable
    public Map<String, String> resolveByPrefix(String prefix, Locale locale) {
        List<LitMessage> messages = messageRepository.findByPrefix(prefix, locale.getLanguage());

        if (messages.isEmpty()) {
            throw new NoDataFoundException();
        }

        return messages.stream()
                .collect(Collectors.toMap(
                        msg -> msg.getId().getLitCode(),
                        LitMessage::getMessage
                ));
    }

    @Override
    @Loggable
    public ByteArrayInputStream exportAllMessagesAsExcel() {
        var messages = messageRepository.findAll();
        try {
            return null;//LitExcelExporter.toExcel(messages);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
}
