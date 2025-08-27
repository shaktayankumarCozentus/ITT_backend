package com.itt.service.repository;

import com.itt.service.entity.LitMessage;
import com.itt.service.entity.LitMessageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LitMessageRepository extends JpaRepository<LitMessage, LitMessageId> {

    @Query("SELECT m FROM LitMessage m WHERE m.id.litCode IN :codes AND m.id.languageCode = :lang")
    List<LitMessage> findAllByLitCodes(@Param("codes") List<String> codes, @Param("lang") String lang);

    List<LitMessage> findByIdLanguageCode(String languageCode);

    @Query("SELECT m FROM LitMessage m WHERE m.id.litCode LIKE CONCAT(:prefix, '%') AND m.id.languageCode = :lang")
    List<LitMessage> findByPrefix(@Param("prefix") String prefix, @Param("lang") String lang);
}
