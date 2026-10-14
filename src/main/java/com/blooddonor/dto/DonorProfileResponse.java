package com.blooddonor.dto;

import com.blooddonor.entity.enums.BloodGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private String city;
    private LocalDate dateOfBirth;
    private Integer age;
    private Integer weightKg;
    private LocalDate lastDonationDate;
    private Boolean available;

    // Eligibility info
    private Boolean eligible;
    private String reason;
    private LocalDate nextEligibleDate;
    private Long daysUntilEligible;
}
