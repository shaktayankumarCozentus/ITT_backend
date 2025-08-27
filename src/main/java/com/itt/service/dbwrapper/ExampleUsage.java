package com.itt.service.dbwrapper;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExampleUsage {




    public void  exampleUsageSchedulerFunction() {



        log.info("Running scheduled task to fetch unsent contract emails...");

        // Fetch 10 earliest unsent emails
       /* List<ContractEmailRequest> pendingEmails = DbExecutor.execute(
                () -> contractEmailRequestRepository.findByIsMailSentFalseOrderByCreatedDtAsc(PageRequest.of(0, 10)),
                "Fetch unsent contract emails"
        ).orElse(Collections.emptyList());

        if (pendingEmails.isEmpty()) {
            log.info("No pending contract emails to process.");
            return;
        }*/


        log.info("Running scheduled task to process contracts...");



    }
}
