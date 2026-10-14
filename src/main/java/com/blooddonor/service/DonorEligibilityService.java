package com.blooddonor.service;

import com.blooddonor.entity.DonorProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Service
public class DonorEligibilityService {

    private final int donationGapDays;

    public DonorEligibilityService(@Value("${app.donation.gap-days:90}") int donationGapDays) {
        this.donationGapDays = donationGapDays;
    }

    public static class EligibilityResult {
        private final boolean eligible;
        private final String reason;
        private final LocalDate nextEligibleDate;
        private final long daysUntilEligible;

        public EligibilityResult(boolean eligible, String reason, LocalDate nextEligibleDate, long daysUntilEligible) {
            this.eligible = eligible;
            this.reason = reason;
            this.nextEligibleDate = nextEligibleDate;
            this.daysUntilEligible = daysUntilEligible;
        }

        public boolean isEligible() {
            return eligible;
        }

        public String getReason() {
            return reason;
        }

        public LocalDate getNextEligibleDate() {
            return nextEligibleDate;
        }

        public long getDaysUntilEligible() {
            return daysUntilEligible;
        }
    }

    public EligibilityResult checkEligibility(DonorProfile donorProfile) {
        return checkEligibility(donorProfile, LocalDate.now());
    }

    public EligibilityResult checkEligibility(DonorProfile donorProfile, LocalDate currentDate) {
        if (donorProfile == null) {
            return new EligibilityResult(false, "Donor profile not found", null, 0);
        }

        // 1. Availability check
        if (Boolean.FALSE.equals(donorProfile.getAvailable())) {
            return new EligibilityResult(false, "Donor has turned off availability", null, 0);
        }

        // 2. Age check (18 - 65)
        if (donorProfile.getDateOfBirth() != null) {
            int age = Period.between(donorProfile.getDateOfBirth(), currentDate).getYears();
            if (age < 18) {
                return new EligibilityResult(false, "Donor must be at least 18 years old (current age: " + age + ")", null, 0);
            }
            if (age > 65) {
                return new EligibilityResult(false, "Donor must be 65 years old or younger (current age: " + age + ")", null, 0);
            }
        } else {
            return new EligibilityResult(false, "Date of birth is missing", null, 0);
        }

        // 3. Weight check (>= 50 kg)
        if (donorProfile.getWeightKg() == null || donorProfile.getWeightKg() < 50) {
            return new EligibilityResult(
                    false,
                    "Donor weight must be at least 50 kg (current weight: " + (donorProfile.getWeightKg() != null ? donorProfile.getWeightKg() : 0) + " kg)",
                    null,
                    0
            );
        }

        // 4. Last donation date gap check
        if (donorProfile.getLastDonationDate() != null) {
            LocalDate nextEligibleDate = donorProfile.getLastDonationDate().plusDays(donationGapDays);
            if (currentDate.isBefore(nextEligibleDate)) {
                long daysUntil = ChronoUnit.DAYS.between(currentDate, nextEligibleDate);
                return new EligibilityResult(
                        false,
                        "Recent donation cooldown active. Next eligible on " + nextEligibleDate + " (" + daysUntil + " days remaining)",
                        nextEligibleDate,
                        daysUntil
                );
            }
        }

        // All checks passed
        return new EligibilityResult(true, null, null, 0);
    }

    public int getDonationGapDays() {
        return donationGapDays;
    }
}
