package com.grievance.service;

import com.grievance.repository.GrievanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    /**
     * Get officer with least active grievances in a department
     */
    public Long getOfficerWithLeastLoad(Long departmentId) {
        try {
            // Call Auth Service to get all officers in department
            List<Map<String, Object>> officers = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/users/officers/available/" + departmentId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(response -> (List<Map<String, Object>>) ((Map<String, Object>) response.get("data")))
                    .block();

            if (officers == null || officers.isEmpty()) {
                log.error("No officers found for department: {}", departmentId);
                return null;
            }

            // Find officer with least active grievances
            Long selectedOfficerId = null;
            Long minLoad = Long.MAX_VALUE;

            for (Map<String, Object> officer : officers) {
                Long officerId = Long.valueOf(officer.get("id").toString());
                Long activeGrievances = grievanceRepository.countActiveGrievancesByOfficer(officerId);

                if (activeGrievances < minLoad) {
                    minLoad = activeGrievances;
                    selectedOfficerId = officerId;
                }
            }

            log.info("Selected officer {} with {} active grievances", selectedOfficerId, minLoad);
            return selectedOfficerId;

        } catch (Exception e) {
            log.error("Error getting officer with least load: {}", e.getMessage());
            return null;
        }
    }
}