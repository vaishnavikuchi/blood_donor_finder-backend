package com.blooddonor.service;

import com.blooddonor.dto.DonorResponseDto;
import com.blooddonor.entity.BloodRequest;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.DonorResponse;
import com.blooddonor.entity.User;
import com.blooddonor.entity.enums.OfferStatus;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.exception.BadRequestException;
import com.blooddonor.exception.ConflictException;
import com.blooddonor.exception.ForbiddenException;
import com.blooddonor.exception.ResourceNotFoundException;
import com.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.repository.DonorProfileRepository;
import com.blooddonor.repository.DonorResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonorResponseService {

    private final DonorResponseRepository donorResponseRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final BloodCompatibilityService compatibilityService;
    private final DonorEligibilityService eligibilityService;

    @Transactional
    public DonorResponseDto offerHelp(Long requestId, User user) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + requestId));

        DonorProfile donor = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Please create a donor profile before offering to donate"));

        // 1. Request must be OPEN
        if (request.getStatus() != RequestStatus.OPEN) {
            throw new BadRequestException("This blood request is not OPEN for offers (current status: " + request.getStatus() + ")");
        }

        // 2. Cannot offer to own request
        if (request.getRequester().getId().equals(user.getId())) {
            throw new BadRequestException("You cannot offer to donate on your own blood request");
        }

        // 3. Check duplicate offer
        if (donorResponseRepository.existsByRequestIdAndDonorId(requestId, donor.getId())) {
            throw new ConflictException("You have already offered to donate for this blood request");
        }

        // 4. Check donor eligibility
        DonorEligibilityService.EligibilityResult eligibility = eligibilityService.checkEligibility(donor);
        if (!eligibility.isEligible()) {
            throw new BadRequestException("Donor is not eligible: " + eligibility.getReason());
        }

        // 5. Check blood group compatibility
        if (!compatibilityService.isCompatible(request.getBloodGroup(), donor.getBloodGroup())) {
            throw new BadRequestException("Blood group incompatible: " + donor.getBloodGroup().getDisplayLabel() +
                    " cannot donate to " + request.getBloodGroup().getDisplayLabel());
        }

        DonorResponse offer = DonorResponse.builder()
                .request(request)
                .donor(donor)
                .status(OfferStatus.OFFERED)
                .build();

        offer = donorResponseRepository.save(offer);
        return mapToDto(offer, true);
    }

    @Transactional(readOnly = true)
    public List<DonorResponseDto> getOffersForRequest(Long requestId, User user) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Blood request not found with id: " + requestId));

        if (!request.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to view offers for this request");
        }

        List<DonorResponse> offers = donorResponseRepository.findByRequestIdWithDetails(requestId);
        return offers.stream()
                .map(offer -> mapToDto(offer, true)) // revealed contact to request owner
                .collect(Collectors.toList());
    }

    @Transactional
    public DonorResponseDto updateOfferStatus(Long offerId, User user, OfferStatus newStatus) {
        DonorResponse offer = donorResponseRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        BloodRequest request = offer.getRequest();

        // Must be the owner of the request
        if (!request.getRequester().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not authorized to update offers on this request");
        }

        if (offer.getStatus() == OfferStatus.DONATED) {
            throw new BadRequestException("This offer has already been marked as DONATED and cannot be modified");
        }

        offer.setStatus(newStatus);

        if (newStatus == OfferStatus.DONATED) {
            DonorProfile donor = offer.getDonor();
            donor.setLastDonationDate(LocalDate.now());
            donorProfileRepository.save(donor);

            int newFulfilled = request.getUnitsFulfilled() + 1;
            request.setUnitsFulfilled(newFulfilled);

            if (newFulfilled >= request.getUnitsNeeded()) {
                request.setStatus(RequestStatus.FULFILLED);
            }
            bloodRequestRepository.save(request);
        }

        offer = donorResponseRepository.save(offer);
        return mapToDto(offer, true);
    }

    @Transactional(readOnly = true)
    public List<DonorResponseDto> getMyOffers(User user) {
        List<DonorResponse> offers = donorResponseRepository.findByDonorUserIdWithDetails(user.getId());
        return offers.stream()
                .map(offer -> mapToDto(offer, false))
                .collect(Collectors.toList());
    }

    public DonorResponseDto mapToDto(DonorResponse offer, boolean revealDonorContact) {
        BloodRequest r = offer.getRequest();
        DonorProfile d = offer.getDonor();

        Integer age = null;
        if (d.getDateOfBirth() != null) {
            age = Period.between(d.getDateOfBirth(), LocalDate.now()).getYears();
        }

        String donorPhone = d.getUser().getPhone();
        String donorEmail = d.getUser().getEmail();

        if (!revealDonorContact) {
            donorPhone = DonorProfileService.maskPhone(donorPhone);
            donorEmail = DonorProfileService.maskEmail(donorEmail);
        }

        return DonorResponseDto.builder()
                .id(offer.getId())
                .requestId(r.getId())
                .patientName(r.getPatientName())
                .hospital(r.getHospital())
                .requestCity(r.getCity())
                .requestBloodGroup(r.getBloodGroup())
                .requestBloodGroupLabel(r.getBloodGroup().getDisplayLabel())
                .urgency(r.getUrgency())
                .requiredBy(r.getRequiredBy())
                .requestStatus(r.getStatus())
                .unitsNeeded(r.getUnitsNeeded())
                .unitsFulfilled(r.getUnitsFulfilled())
                .requestContactPhone(r.getContactPhone())
                .donorId(d.getId())
                .donorUserId(d.getUser().getId())
                .donorName(d.getUser().getName())
                .donorBloodGroup(d.getBloodGroup())
                .donorBloodGroupLabel(d.getBloodGroup().getDisplayLabel())
                .donorCity(d.getCity())
                .donorPhone(donorPhone)
                .donorEmail(donorEmail)
                .donorAge(age)
                .donorWeightKg(d.getWeightKg())
                .donorLastDonationDate(d.getLastDonationDate())
                .status(offer.getStatus())
                .createdAt(offer.getCreatedAt())
                .build();
    }
}
