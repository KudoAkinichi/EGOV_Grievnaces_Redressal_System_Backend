package com.grievance.service;

import com.grievance.repository.GrievanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssignmentService {

    private final GrievanceRepository grievanceRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${internal.service.token}")
    private String internalServiceToken;


    public Long getOfficerWithLeastLoad(Long departmentId) {
        try {
            List<Map<String, Object>> officers = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/users/officers/available/" + departmentId)
                    .header("X-INTERNAL-TOKEN", internalServiceToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> (List<Map<String, Object>>) response.get("data"))
                    .block();


            if (officers == null || officers.isEmpty()) {
                log.warn("No officers found for department {}", departmentId);
                return null;
            }

            Long selectedOfficerId = null;
            Long minLoad = Long.MAX_VALUE;

            for (Map<String, Object> officer : officers) {
                Long officerId = Long.valueOf(officer.get("id").toString());
                Long activeCount =
                        grievanceRepository.countActiveGrievancesByOfficer(officerId);

                if (activeCount < minLoad) {
                    minLoad = activeCount;
                    selectedOfficerId = officerId;
                }
            }

            log.info("Auto-assigned officer {} with {} active grievances",
                    selectedOfficerId, minLoad);

            return selectedOfficerId;

        } catch (Exception e) {
            log.error("Error getting officer with least load", e);
            return null;
        }
    }
}
