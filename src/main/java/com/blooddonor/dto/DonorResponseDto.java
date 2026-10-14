package com.blooddonor.dto;

import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.OfferStatus;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.entity.enums.Urgency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorResponseDto {
    private Long id;
    
    // Request info
    private Long requestId;
    private String patientName;
    private String hospital;
    private String requestCity;
    private BloodGroup requestBloodGroup;
    private String requestBloodGroupLabel;
    private Urgency urgency;
    private LocalDateTime requiredBy;
    private RequestStatus requestStatus;
    private Integer unitsNeeded;
    private Integer unitsFulfilled;
    private String requestContactPhone;

    // Donor info
    private Long donorId;
    private Long donorUserId;
    private String donorName;
    private BloodGroup donorBloodGroup;
    private String donorBloodGroupLabel;
    private String donorCity;
    private String donorPhone;
    private String donorEmail;
    private Integer donorAge;
    private Integer donorWeightKg;
    private LocalDate donorLastDonationDate;

    // Offer details
    private OfferStatus status;
    private LocalDateTime createdAt;
}
