package com.blooddonor.dto;

import com.blooddonor.entity.enums.OfferStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferStatusUpdateDto {
    @NotNull(message = "Status is required")
    private OfferStatus status;
}
