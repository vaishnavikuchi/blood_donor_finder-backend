package com.blooddonor.service;

import com.blooddonor.dto.StatsResponseDto;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.repository.DonorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final DonorProfileRepository donorProfileRepository;
    private final BloodRequestRepository bloodRequestRepository;

    @Transactional(readOnly = true)
    public StatsResponseDto getStats() {
        long totalDonors = donorProfileRepository.count();
        long openRequests = bloodRequestRepository.countByStatus(RequestStatus.OPEN);
        long fulfilledRequests = bloodRequestRepository.countByStatus(RequestStatus.FULFILLED);
        long totalUnitsDonated = bloodRequestRepository.sumTotalUnitsDonated();

        Map<String, Long> donorsByBloodGroup = new LinkedHashMap<>();
        for (BloodGroup bg : BloodGroup.values()) {
            long count = donorProfileRepository.countByBloodGroup(bg);
            donorsByBloodGroup.put(bg.getDisplayLabel(), count);
        }

        return StatsResponseDto.builder()
                .totalDonors(totalDonors)
                .openRequests(openRequests)
                .fulfilledRequests(fulfilledRequests)
                .totalUnitsDonated(totalUnitsDonated)
                .donorsByBloodGroup(donorsByBloodGroup)
                .build();
    }
}
