package com.grievance.scheduler;

import com.grievance.common.enums.GrievanceStatus;
import com.grievance.messaging.RabbitMQProducer;
import com.grievance.model.Grievance;
import com.grievance.model.GrievanceStatusHistory;
import com.grievance.repository.GrievanceRepository;
import com.grievance.repository.GrievanceStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EscalationScheduler {

    private final GrievanceRepository grievanceRepository;
    private final GrievanceStatusHistoryRepository statusHistoryRepository;
    private final RabbitMQProducer rabbitMQProducer;

    /**
     * Run every hour to check for grievances that need auto-escalation
     */
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    @Transactional
    public void autoEscalateGrievances() {
        log.info("Running auto-escalation scheduler...");

        LocalDateTime currentTime = LocalDateTime.now();
        List<Grievance> grievancesToEscalate = grievanceRepository
                .findGrievancesForAutoEscalation(currentTime);

        if (grievancesToEscalate.isEmpty()) {
            log.info("No grievances to auto-escalate");
            return;
        }

        log.info("Found {} grievances to auto-escalate", grievancesToEscalate.size());

        for (Grievance grievance : grievancesToEscalate) {
            try {
                escalateGrievance(grievance);
            } catch (Exception e) {
                log.error("Failed to auto-escalate grievance {}: {}",
                        grievance.getGrievanceNumber(), e.getMessage());
            }
        }

        log.info("Auto-escalation completed");
    }

    private void escalateGrievance(Grievance grievance) {
        GrievanceStatus oldStatus = grievance.getStatus();
        grievance.setStatus(GrievanceStatus.ESCALATED);
        grievance.setEscalatedAt(LocalDateTime.now());

        grievanceRepository.save(grievance);

        // Record status change
        GrievanceStatusHistory history = new GrievanceStatusHistory();
        history.setGrievanceId(grievance.getId());
        history.setOldStatus(oldStatus);
        history.setNewStatus(GrievanceStatus.ESCALATED);
        history.setChangedBy(0L); // System user
        history.setRemarks("Auto-escalated due to timeout");
        statusHistoryRepository.save(history);

        // Send notification to citizen
        sendNotification(grievance.getCitizenId(),
                "Grievance Auto-Escalated",
                String.format("Your grievance %s has been auto-escalated to a supervisor due to timeout.",
                        grievance.getGrievanceNumber()));

        // Send notification to supervisor
        if (grievance.getEscalatedToSupervisorId() != null) {
            sendNotification(grievance.getEscalatedToSupervisorId(),
                    "Grievance Escalated",
                    String.format("Grievance %s has been escalated to you.",
                            grievance.getGrievanceNumber()));
        }

        log.info("Auto-escalated grievance: {}", grievance.getGrievanceNumber());
    }

    private void sendNotification(Long userId, String subject, String body) {
        Map<String, String> emailData = new HashMap<>();
        emailData.put("userId", userId.toString());
        emailData.put("subject", subject);
        emailData.put("body", body);
        rabbitMQProducer.sendNotification(emailData);
    }
}