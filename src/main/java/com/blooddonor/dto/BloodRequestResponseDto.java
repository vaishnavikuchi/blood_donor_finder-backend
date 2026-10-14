package com.blooddonor.dto;

import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.entity.enums.Urgency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestResponseDto {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private String patientName;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private Integer unitsNeeded;
    private Integer unitsFulfilled;
    private String hospital;
    private String city;
    private Urgency urgency;
    private LocalDateTime requiredBy;
    private String contactPhone;
    private String note;
    private RequestStatus status;
    private LocalDateTime createdAt;
    private Integer responseCount;
    private Boolean isOwner;
}
