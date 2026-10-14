package com.blooddonor.dto;

import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.OfferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchedDonorDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private BloodGroup bloodGroup;
    private String bloodGroupLabel;
    private String city;
    private Integer age;
    private Integer weightKg;
    private LocalDate lastDonationDate;
    private Boolean available;
    private Boolean isExactMatch;
    private Boolean isSameCity;
    private Boolean hasOffered;
    private OfferStatus offerStatus;
    private Long offerId;
}
