package com.blooddonor.service;

import com.blooddonor.dto.BloodRequestCreateDto;
import com.blooddonor.dto.BloodRequestResponseDto;
import com.blooddonor.dto.BloodRequestUpdateDto;
import com.blooddonor.dto.MatchedDonorDto;
import com.blooddonor.entity.BloodRequest;
import com.blooddonor.entity.User;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.entity.enums.Urgency;
import com.blooddonor.exception.BadRequestException;
import com.blooddonor.exception.ForbiddenException;
import com.blooddonor.exception.ResourceNotFoundException;
import com.blooddonor.repository.BloodRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BloodRequestService {

    private final BloodRequestRepository bloodRequestRepository;
    private final MatchingService matchingService;

    @Transactional
    public BloodRequestResponseDto createRequest(User requester, BloodRequestCreateDto dto) {
        if (dto.getRequiredBy().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Required by date/time must be in the future");
        }

        BloodRequest request = BloodRequest.builder()
                .requester(requester)
                .patientName(dto.getPatientName().trim())
                .bloodGroup(dto.getBloodGroup())
                .unitsNeeded(dto.getUnitsNeeded())
                .unitsFulfilled(0)
                .hospital(dto.getHospital().trim())
                .city(dto.getCity().trim())
                .urgency(dto.getUrgency())
                .requiredBy(dto.getRequiredBy())
                .contactPhone(dto.getContactPhone().trim())
                .note(dto.getNote())
                .status(RequestStatus.OPEN)
                .build();

        request = bloodRequestRepository.save(request);
        return mapToDto(request, requester);
    }

    @Transactional(readOnly = true)
    public List<BloodRequestResponseDto> getRequests(
            BloodGroup bloodGroup,
            String city,
            Urgency urgency,
            RequestStatus status,
            User currentUser
    ) {
        String cityParam = (city != null && !city.trim().isEmpty()) ? city.trim() : null;
        List<BloodRequest> list = bloodRequestRepository.filterRequests(bloodGroup, cityParam, urgency, status);

        return list.stream()
                .map(r -> mapToDto(r, currentUser))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BloodRequestResponseDto> getMyRequests(User user) {
        List<BloodRequest> list = bloodRequestRepository.findByRequesterIdOrderByCreatedAtDesc(user.getId());
        return list.stream()
                .map(r -> mapToDto(r, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BloodRequestResponseDto getRequestById(Long id, User currentUser) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));
        return mapToDto(request, currentUser);
    }

    @Transactional
    public BloodRequestResponseDto updateRequest(Long id, User user, BloodRequestUpdateDto dto) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));

        // Check ownership
        if (!request.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to update this blood request");
        }

        // Only allowed while OPEN
        if (request.getStatus() != RequestStatus.OPEN) {
            throw new BadRequestException("Only OPEN requests can be updated (current status: " + request.getStatus() + ")");
        }

        if (dto.getUnitsNeeded() < request.getUnitsFulfilled()) {
            throw new BadRequestException("Units needed cannot be less than already fulfilled units (" + request.getUnitsFulfilled() + ")");
        }

        request.setPatientName(dto.getPatientName().trim());
        request.setBloodGroup(dto.getBloodGroup());
        request.setUnitsNeeded(dto.getUnitsNeeded());
        request.setHospital(dto.getHospital().trim());
        request.setCity(dto.getCity().trim());
        request.setUrgency(dto.getUrgency());
        request.setRequiredBy(dto.getRequiredBy());
        request.setContactPhone(dto.getContactPhone().trim());
        request.setNote(dto.getNote());

        request = bloodRequestRepository.save(request);
        return mapToDto(request, user);
    }

    @Transactional
    public BloodRequestResponseDto cancelRequest(Long id, User user) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));

        // Only owner can cancel
        if (!request.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to cancel this blood request");
        }

        if (request.getStatus() == RequestStatus.CANCELLED) {
            throw new BadRequestException("Request is already cancelled");
        }
        if (request.getStatus() == RequestStatus.FULFILLED) {
            throw new BadRequestException("Cannot cancel an already fulfilled request");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request = bloodRequestRepository.save(request);
        return mapToDto(request, user);
    }

    @Transactional(readOnly = true)
    public List<MatchedDonorDto> getMatchesForRequest(Long id, User user, boolean includeOtherCities) {
        BloodRequest request = bloodRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + id));

        // Owner only
        if (!request.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to view matches for this blood request");
        }

        return matchingService.findMatchesForRequest(request, includeOtherCities);
    }

    public BloodRequestResponseDto mapToDto(BloodRequest r, User currentUser) {
        boolean isOwner = currentUser != null && r.getRequester().getId().equals(currentUser.getId());
        
        // Privacy: The request's contact_phone is visible to logged-in users.
        String phone = r.getContactPhone();
        if (currentUser == null) {
            phone = DonorProfileService.maskPhone(phone);
        }

        int responseCount = r.getResponses() != null ? r.getResponses().size() : 0;

        return BloodRequestResponseDto.builder()
                .id(r.getId())
                .requesterId(r.getRequester().getId())
                .requesterName(r.getRequester().getName())
                .patientName(r.getPatientName())
                .bloodGroup(r.getBloodGroup())
                .bloodGroupLabel(r.getBloodGroup().getDisplayLabel())
                .unitsNeeded(r.getUnitsNeeded())
                .unitsFulfilled(r.getUnitsFulfilled())
                .hospital(r.getHospital())
                .city(r.getCity())
                .urgency(r.getUrgency())
                .requiredBy(r.getRequiredBy())
                .contactPhone(phone)
                .note(r.getNote())
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .responseCount(responseCount)
                .isOwner(isOwner)
                .build();
    }
}
