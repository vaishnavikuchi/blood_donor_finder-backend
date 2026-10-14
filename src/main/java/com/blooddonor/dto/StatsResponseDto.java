package com.blooddonor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponseDto {
    private long totalDonors;
    private long openRequests;
    private long fulfilledRequests;
    private long totalUnitsDonated;
    private Map<String, Long> donorsByBloodGroup;
}
