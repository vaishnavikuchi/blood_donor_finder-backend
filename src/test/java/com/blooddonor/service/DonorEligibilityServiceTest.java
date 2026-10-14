package com.blooddonor.service;

import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.enums.BloodGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Donor Eligibility Service Tests")
class DonorEligibilityServiceTest {

    private DonorEligibilityService eligibilityService;
    private final LocalDate referenceDate = LocalDate.of(2026, 10, 1);

    @BeforeEach
    void setUp() {
        eligibilityService = new DonorEligibilityService(90);
    }

    private DonorProfile createValidDonor() {
        return DonorProfile.builder()
                .bloodGroup(BloodGroup.O_POS)
                .city("New York")
                .dateOfBirth(referenceDate.minusYears(25)) // 25 years old
                .weightKg(70)
                .available(true)
                .lastDonationDate(null)
                .build();
    }

    @Test
    @DisplayName("Eligible when never donated before")
    void testNeverDonated() {
        DonorProfile donor = createValidDonor();
        donor.setLastDonationDate(null);

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertTrue(result.isEligible());
        assertNull(result.getReason());
        assertNull(result.getNextEligibleDate());
        assertEquals(0, result.getDaysUntilEligible());
    }

    @Test
    @DisplayName("Ineligible when donated 89 days ago (1 day before 90-day cooldown expires)")
    void testDonated89DaysAgo() {
        DonorProfile donor = createValidDonor();
        donor.setLastDonationDate(referenceDate.minusDays(89));

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertFalse(result.isEligible());
        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("Recent donation cooldown active"));
        assertEquals(referenceDate.plusDays(1), result.getNextEligibleDate());
        assertEquals(1, result.getDaysUntilEligible());
    }

    @Test
    @DisplayName("Eligible when donated exactly 90 days ago")
    void testDonated90DaysAgo() {
        DonorProfile donor = createValidDonor();
        donor.setLastDonationDate(referenceDate.minusDays(90));

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertTrue(result.isEligible());
        assertNull(result.getReason());
    }

    @Test
    @DisplayName("Eligible when donated more than 90 days ago (e.g., 100 days)")
    void testDonated100DaysAgo() {
        DonorProfile donor = createValidDonor();
        donor.setLastDonationDate(referenceDate.minusDays(100));

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertTrue(result.isEligible());
    }

    @Test
    @DisplayName("Ineligible when underage (less than 18 years old)")
    void testUnderageDonor() {
        DonorProfile donor = createValidDonor();
        donor.setDateOfBirth(referenceDate.minusYears(17)); // 17 years old

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertFalse(result.isEligible());
        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("at least 18 years old"));
    }

    @Test
    @DisplayName("Ineligible when over maximum age (> 65 years old)")
    void testOverMaxAgeDonor() {
        DonorProfile donor = createValidDonor();
        donor.setDateOfBirth(referenceDate.minusYears(66)); // 66 years old

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertFalse(result.isEligible());
        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("65 years old or younger"));
    }

    @Test
    @DisplayName("Ineligible when underweight (less than 50 kg)")
    void testUnderweightDonor() {
        DonorProfile donor = createValidDonor();
        donor.setWeightKg(48); // 48 kg

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertFalse(result.isEligible());
        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("at least 50 kg"));
    }

    @Test
    @DisplayName("Ineligible when donor turned off availability")
    void testUnavailableDonor() {
        DonorProfile donor = createValidDonor();
        donor.setAvailable(false);

        DonorEligibilityService.EligibilityResult result = eligibilityService.checkEligibility(donor, referenceDate);
        assertFalse(result.isEligible());
        assertNotNull(result.getReason());
        assertTrue(result.getReason().contains("turned off availability"));
    }
}
