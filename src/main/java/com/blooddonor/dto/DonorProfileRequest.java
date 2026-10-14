package com.blooddonor.dto;

import com.blooddonor.entity.enums.BloodGroup;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonorProfileRequest {

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotBlank(message = "City is required")
    @Size(max = 60, message = "City must not exceed 60 characters")
    private String city;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Weight (kg) is required")
    @Min(value = 30, message = "Weight must be at least 30 kg")
    @Max(value = 300, message = "Weight must not exceed 300 kg")
    private Integer weightKg;

    private LocalDate lastDonationDate;

    private Boolean available;
}
