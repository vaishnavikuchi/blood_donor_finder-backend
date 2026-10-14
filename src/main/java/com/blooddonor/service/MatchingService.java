package com.blooddonor.service;

import com.blooddonor.dto.MatchedDonorDto;
import com.blooddonor.entity.BloodRequest;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.DonorResponse;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.repository.DonorProfileRepository;
import com.blooddonor.repository.DonorResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final DonorProfileRepository donorProfileRepository;
    private final BloodCompatibilityService compatibilityService;
    private final DonorEligibilityService eligibilityService;
    private final DonorResponseRepository donorResponseRepository;

    @Transactional(readOnly = true)
    public List<MatchedDonorDto> findMatchesForRequest(BloodRequest request, boolean includeOtherCities) {
        if (request == null) {
            return Collections.emptyList();
        }

        Long requesterUserId = request.getRequester() != null ? request.getRequester().getId() : null;
        BloodGroup targetBloodGroup = request.getBloodGroup();
        String targetCity = request.getCity() != null ? request.getCity().trim() : "";

        // Get all donors with user loaded
        List<DonorProfile> allDonors = donorProfileRepository.findAllWithUser();

        // Load existing offers for this request to annotate MatchedDonorDto
        List<DonorResponse> existingOffers = donorResponseRepository.findByRequestIdWithDetails(request.getId());
        Map<Long, DonorResponse> offersByDonorId = existingOffers.stream()
                .collect(Collectors.toMap(offer -> offer.getDonor().getId(), offer -> offer, (o1, o2) -> o1));

        // Filter:
        // 1. Exclude requester themselves
        // 2. Compatible blood group
        // 3. Eligible donor
        List<DonorProfile> eligibleCompatibleDonors = allDonors.stream()
                .filter(d -> requesterUserId == null || !d.getUser().getId().equals(requesterUserId))
                .filter(d -> compatibilityService.isCompatible(targetBloodGroup, d.getBloodGroup()))
                .filter(d -> eligibilityService.checkEligibility(d).isEligible())
                .collect(Collectors.toList());

        // Comparator:
        // Exact blood group first, then other compatible groups,
        // then longest time since last donation (null lastDonationDate comes first, else oldest date first)
        Comparator<DonorProfile> donorComparator = (d1, d2) -> {
            boolean d1Exact = d1.getBloodGroup() == targetBloodGroup;
            boolean d2Exact = d2.getBloodGroup() == targetBloodGroup;
            if (d1Exact != d2Exact) {
                return d1Exact ? -1 : 1;
            }

            if (d1.getLastDonationDate() == null && d2.getLastDonationDate() == null) {
                return 0;
            }
            if (d1.getLastDonationDate() == null) {
                return -1;
            }
            if (d2.getLastDonationDate() == null) {
                return 1;
            }
            return d1.getLastDonationDate().compareTo(d2.getLastDonationDate());
        };

        // Split into same-city and other-city
        List<DonorProfile> sameCityDonors = new ArrayList<>();
        List<DonorProfile> otherCityDonors = new ArrayList<>();

        for (DonorProfile donor : eligibleCompatibleDonors) {
            String donorCity = donor.getCity() != null ? donor.getCity().trim() : "";
            if (donorCity.equalsIgnoreCase(targetCity)) {
                sameCityDonors.add(donor);
            } else {
                otherCityDonors.add(donor);
            }
        }

        sameCityDonors.sort(donorComparator);
        otherCityDonors.sort(donorComparator);

        List<DonorProfile> resultList = new ArrayList<>(sameCityDonors);
        if (includeOtherCities) {
            resultList.addAll(otherCityDonors);
        }

        return resultList.stream().map(donor -> {
            boolean isSameCity = donor.getCity() != null && donor.getCity().trim().equalsIgnoreCase(targetCity);
            boolean isExactMatch = donor.getBloodGroup() == targetBloodGroup;
            DonorResponse existingOffer = offersByDonorId.get(donor.getId());

            Integer age = null;
            if (donor.getDateOfBirth() != null) {
                age = Period.between(donor.getDateOfBirth(), LocalDate.now()).getYears();
            }

            return MatchedDonorDto.builder()
                    .id(donor.getId())
                    .userId(donor.getUser().getId())
                    .name(donor.getUser().getName())
                    .email(donor.getUser().getEmail())
                    .phone(donor.getUser().getPhone())
                    .bloodGroup(donor.getBloodGroup())
                    .bloodGroupLabel(donor.getBloodGroup().getDisplayLabel())
                    .city(donor.getCity())
                    .age(age)
                    .weightKg(donor.getWeightKg())
                    .lastDonationDate(donor.getLastDonationDate())
                    .available(donor.getAvailable())
                    .isExactMatch(isExactMatch)
                    .isSameCity(isSameCity)
                    .hasOffered(existingOffer != null)
                    .offerStatus(existingOffer != null ? existingOffer.getStatus() : null)
                    .offerId(existingOffer != null ? existingOffer.getId() : null)
                    .build();
        }).collect(Collectors.toList());
    }
}
