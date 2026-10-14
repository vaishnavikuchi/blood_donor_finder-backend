package com.blooddonor.service;

import com.blooddonor.entity.BloodRequest;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.repository.BloodRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestExpirationScheduler {

    private final BloodRequestRepository bloodRequestRepository;

    @Scheduled(fixedRate = 900000) // Runs every 15 minutes (900,000 ms)
    @Transactional
    public void expirePassedRequests() {
        LocalDateTime now = LocalDateTime.now();
        List<BloodRequest> expiredList = bloodRequestRepository.findByStatusAndRequiredByBefore(RequestStatus.OPEN, now);

        if (!expiredList.isEmpty()) {
            log.info("Found {} OPEN requests past required_by date. Marking as EXPIRED.", expiredList.size());
            for (BloodRequest request : expiredList) {
                request.setStatus(RequestStatus.EXPIRED);
            }
            bloodRequestRepository.saveAll(expiredList);
        }
    }
}
