package com.blooddonor.controller;

import com.blooddonor.dto.*;
import com.blooddonor.entity.User;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.entity.enums.Urgency;
import com.blooddonor.service.BloodRequestService;
import com.blooddonor.service.DonorResponseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;
    private final DonorResponseService donorResponseService;

    @PostMapping
    public ResponseEntity<BloodRequestResponseDto> createRequest(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BloodRequestCreateDto dto
    ) {
        BloodRequestResponseDto response = bloodRequestService.createRequest(user, dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BloodRequestResponseDto>> getRequests(
            @RequestParam(required = false) BloodGroup bloodGroup,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Urgency urgency,
            @RequestParam(required = false) RequestStatus status,
            @AuthenticationPrincipal User user
    ) {
        List<BloodRequestResponseDto> responses = bloodRequestService.getRequests(bloodGroup, city, urgency, status, user);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my")
    public ResponseEntity<List<BloodRequestResponseDto>> getMyRequests(@AuthenticationPrincipal User user) {
        List<BloodRequestResponseDto> responses = bloodRequestService.getMyRequests(user);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<BloodRequestResponseDto> getRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        BloodRequestResponseDto response = bloodRequestService.getRequestById(id, user);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<BloodRequestResponseDto> updateRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BloodRequestUpdateDto dto
    ) {
        BloodRequestResponseDto response = bloodRequestService.updateRequest(id, user, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id:[0-9]+}/cancel")
    public ResponseEntity<BloodRequestResponseDto> cancelRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        BloodRequestResponseDto response = bloodRequestService.cancelRequest(id, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id:[0-9]+}/matches")
    public ResponseEntity<List<MatchedDonorDto>> getMatches(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean includeOtherCities,
            @AuthenticationPrincipal User user
    ) {
        List<MatchedDonorDto> matches = bloodRequestService.getMatchesForRequest(id, user, includeOtherCities);
        return ResponseEntity.ok(matches);
    }

    @PostMapping("/{id:[0-9]+}/offers")
    public ResponseEntity<DonorResponseDto> offerHelp(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        DonorResponseDto response = donorResponseService.offerHelp(id, user);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id:[0-9]+}/offers")
    public ResponseEntity<List<DonorResponseDto>> getOffersForRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        List<DonorResponseDto> offers = donorResponseService.getOffersForRequest(id, user);
        return ResponseEntity.ok(offers);
    }
}
