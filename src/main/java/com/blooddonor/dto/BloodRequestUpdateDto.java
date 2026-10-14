package com.blooddonor.dto;

import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.Urgency;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestUpdateDto {

    @NotBlank(message = "Patient name is required")
    @Size(max = 100, message = "Patient name must not exceed 100 characters")
    private String patientName;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotNull(message = "Units needed is required")
    @Min(value = 1, message = "Units needed must be at least 1")
    @Max(value = 10, message = "Units needed cannot exceed 10")
    private Integer unitsNeeded;

    @NotBlank(message = "Hospital is required")
    @Size(max = 150, message = "Hospital must not exceed 150 characters")
    private String hospital;

    @NotBlank(message = "City is required")
    @Size(max = 60, message = "City must not exceed 60 characters")
    private String city;

    @NotNull(message = "Urgency is required")
    private Urgency urgency;

    @NotNull(message = "Required by date/time is required")
    private LocalDateTime requiredBy;

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    private String note;
}
