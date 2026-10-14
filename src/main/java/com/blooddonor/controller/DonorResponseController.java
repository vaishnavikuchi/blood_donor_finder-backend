package com.blooddonor.controller;

import com.blooddonor.dto.DonorResponseDto;
import com.blooddonor.dto.OfferStatusUpdateDto;
import com.blooddonor.entity.User;
import com.blooddonor.service.DonorResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@RequiredArgsConstructor
public class DonorResponseController {

    private final DonorResponseService donorResponseService;

    @PatchMapping("/{offerId:[0-9]+}/status")
    public ResponseEntity<DonorResponseDto> updateOfferStatus(
            @PathVariable Long offerId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OfferStatusUpdateDto dto
    ) {
        DonorResponseDto response = donorResponseService.updateOfferStatus(offerId, user, dto.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<DonorResponseDto>> getMyOffers(@AuthenticationPrincipal User user) {
        List<DonorResponseDto> responses = donorResponseService.getMyOffers(user);
        return ResponseEntity.ok(responses);
    }
}
