package com.blooddonor.service;

import com.blooddonor.dto.DonorProfileRequest;
import com.blooddonor.dto.DonorProfileResponse;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.User;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.exception.BadRequestException;
import com.blooddonor.exception.ConflictException;
import com.blooddonor.exception.ResourceNotFoundException;
import com.blooddonor.repository.DonorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonorProfileService {

    private final DonorProfileRepository donorProfileRepository;
    private final BloodCompatibilityService compatibilityService;
    private final DonorEligibilityService eligibilityService;

    @Transactional
    public DonorProfileResponse createProfile(User user, DonorProfileRequest request) {
        if (donorProfileRepository.existsByUserId(user.getId())) {
            throw new ConflictException("User already has a donor profile");
        }

        validateDonorCriteria(request.getDateOfBirth(), request.getWeightKg());

        DonorProfile profile = DonorProfile.builder()
                .user(user)
                .bloodGroup(request.getBloodGroup())
                .city(request.getCity().trim())
                .dateOfBirth(request.getDateOfBirth())
                .weightKg(request.getWeightKg())
                .lastDonationDate(request.getLastDonationDate())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .build();

        profile = donorProfileRepository.save(profile);
        return mapToResponse(profile, false);
    }

    @Transactional(readOnly = true)
    public DonorProfileResponse getMyProfile(User user) {
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found for user: " + user.getEmail()));
        return mapToResponse(profile, false);
    }

    @Transactional
    public DonorProfileResponse updateProfile(User user, DonorProfileRequest request) {
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        validateDonorCriteria(request.getDateOfBirth(), request.getWeightKg());

        profile.setBloodGroup(request.getBloodGroup());
        profile.setCity(request.getCity().trim());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setWeightKg(request.getWeightKg());
        profile.setLastDonationDate(request.getLastDonationDate());
        if (request.getAvailable() != null) {
            profile.setAvailable(request.getAvailable());
        }

        profile = donorProfileRepository.save(profile);
        return mapToResponse(profile, false);
    }

    @Transactional
    public DonorProfileResponse toggleAvailability(User user) {
        DonorProfile profile = donorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found"));

        profile.setAvailable(!Boolean.TRUE.equals(profile.getAvailable()));
        profile = donorProfileRepository.save(profile);
        return mapToResponse(profile, false);
    }

    @Transactional(readOnly = true)
    public List<DonorProfileResponse> searchDonors(BloodGroup bloodGroup, String city, Boolean eligibleOnly) {
        List<DonorProfile> allDonors = donorProfileRepository.findAllWithUser();

        Set<BloodGroup> allowedGroups = null;
        if (bloodGroup != null) {
            // Searching for a patient's blood group returns donors of all compatible groups
            allowedGroups = compatibilityService.getCompatibleDonorGroups(bloodGroup);
        }

        final Set<BloodGroup> filterGroups = allowedGroups;
        final String searchCity = (city != null && !city.trim().isEmpty()) ? city.trim().toLowerCase() : null;

        return allDonors.stream()
                .filter(donor -> {
                    // Filter blood group
                    if (filterGroups != null && !filterGroups.contains(donor.getBloodGroup())) {
                        return false;
                    }
                    // Filter city
                    if (searchCity != null && !donor.getCity().toLowerCase().contains(searchCity)) {
                        return false;
                    }
                    // Filter eligibleOnly
                    if (Boolean.TRUE.equals(eligibleOnly)) {
                        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor);
                        if (!result.isEligible()) {
                            return false;
                        }
                    }
                    return true;
                })
                .map(donor -> mapToResponse(donor, true)) // masked in public search
                .collect(Collectors.toList());
    }

    public DonorProfileResponse mapToResponse(DonorProfile profile, boolean maskPrivacy) {
        DonorEligibilityService.EligibilityResult eligibility = eligibilityService.checkEligibility(profile);

        Integer age = null;
        if (profile.getDateOfBirth() != null) {
            age = Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears();
        }

        String email = profile.getUser().getEmail();
        String phone = profile.getUser().getPhone();

        if (maskPrivacy) {
            email = maskEmail(email);
            phone = maskPhone(phone);
        }

        return DonorProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .name(profile.getUser().getName())
                .email(email)
                .phone(phone)
                .bloodGroup(profile.getBloodGroup())
                .bloodGroupLabel(profile.getBloodGroup().getDisplayLabel())
                .city(profile.getCity())
                .dateOfBirth(profile.getDateOfBirth())
                .age(age)
                .weightKg(profile.getWeightKg())
                .lastDonationDate(profile.getLastDonationDate())
                .available(profile.getAvailable())
                .eligible(eligibility.isEligible())
                .reason(eligibility.getReason())
                .nextEligibleDate(eligibility.getNextEligibleDate())
                .daysUntilEligible(eligibility.getDaysUntilEligible())
                .build();
    }

    private void validateDonorCriteria(LocalDate dateOfBirth, Integer weightKg) {
        if (dateOfBirth != null) {
            int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
            if (age < 18 || age > 65) {
                throw new BadRequestException("Donor must be between 18 and 65 years old (current age: " + age + ")");
            }
        }
        if (weightKg != null && weightKg < 50) {
            throw new BadRequestException("Donor weight must be at least 50 kg (provided: " + weightKg + " kg)");
        }
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***@***.***";
        }
        int atIndex = email.indexOf('@');
        String namePart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);

        if (namePart.length() <= 2) {
            return namePart.charAt(0) + "***" + domainPart;
        }
        return namePart.charAt(0) + "***" + namePart.charAt(namePart.length() - 1) + domainPart;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "******";
        }
        int len = phone.length();
        String suffix = phone.substring(len - 4);
        return "******" + suffix;
    }
}
