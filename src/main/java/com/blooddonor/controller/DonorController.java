package com.blooddonor.controller;

import com.blooddonor.dto.DonorProfileRequest;
import com.blooddonor.dto.DonorProfileResponse;
import com.blooddonor.entity.User;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.service.DonorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorProfileService donorProfileService;

    @PostMapping("/profile")
    public ResponseEntity<DonorProfileResponse> createProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DonorProfileRequest request
    ) {
        DonorProfileResponse response = donorProfileService.createProfile(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<DonorProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        DonorProfileResponse response = donorProfileService.getMyProfile(user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<DonorProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DonorProfileRequest request
    ) {
        DonorProfileResponse response = donorProfileService.updateProfile(user, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile/availability")
    public ResponseEntity<DonorProfileResponse> toggleAvailability(@AuthenticationPrincipal User user) {
        DonorProfileResponse response = donorProfileService.toggleAvailability(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<DonorProfileResponse>> searchDonors(
            @RequestParam(required = false) BloodGroup bloodGroup,
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "false") Boolean eligibleOnly
    ) {
        List<DonorProfileResponse> responses = donorProfileService.searchDonors(bloodGroup, city, eligibleOnly);
        return ResponseEntity.ok(responses);
    }
}
