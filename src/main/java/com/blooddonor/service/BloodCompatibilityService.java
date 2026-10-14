package com.blooddonor.service;

import com.blooddonor.entity.enums.BloodGroup;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BloodCompatibilityService {

    private static final Map<BloodGroup, Set<BloodGroup>> COMPATIBILITY_MAP = new EnumMap<>(BloodGroup.class);

    static {
        // O- can receive from: O-
        COMPATIBILITY_MAP.put(BloodGroup.O_NEG, EnumSet.of(BloodGroup.O_NEG));

        // O+ can receive from: O-, O+
        COMPATIBILITY_MAP.put(BloodGroup.O_POS, EnumSet.of(BloodGroup.O_NEG, BloodGroup.O_POS));

        // A- can receive from: O-, A-
        COMPATIBILITY_MAP.put(BloodGroup.A_NEG, EnumSet.of(BloodGroup.O_NEG, BloodGroup.A_NEG));

        // A+ can receive from: O-, O+, A-, A+
        COMPATIBILITY_MAP.put(BloodGroup.A_POS, EnumSet.of(
                BloodGroup.O_NEG, BloodGroup.O_POS, BloodGroup.A_NEG, BloodGroup.A_POS
        ));

        // B- can receive from: O-, B-
        COMPATIBILITY_MAP.put(BloodGroup.B_NEG, EnumSet.of(BloodGroup.O_NEG, BloodGroup.B_NEG));

        // B+ can receive from: O-, O+, B-, B+
        COMPATIBILITY_MAP.put(BloodGroup.B_POS, EnumSet.of(
                BloodGroup.O_NEG, BloodGroup.O_POS, BloodGroup.B_NEG, BloodGroup.B_POS
        ));

        // AB- can receive from: O-, A-, B-, AB-
        COMPATIBILITY_MAP.put(BloodGroup.AB_NEG, EnumSet.of(
                BloodGroup.O_NEG, BloodGroup.A_NEG, BloodGroup.B_NEG, BloodGroup.AB_NEG
        ));

        // AB+ can receive from: all 8 groups
        COMPATIBILITY_MAP.put(BloodGroup.AB_POS, EnumSet.allOf(BloodGroup.class));
    }

    public Set<BloodGroup> getCompatibleDonorGroups(BloodGroup recipientGroup) {
        if (recipientGroup == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(COMPATIBILITY_MAP.getOrDefault(recipientGroup, Collections.emptySet()));
    }

    public boolean isCompatible(BloodGroup recipientGroup, BloodGroup donorGroup) {
        if (recipientGroup == null || donorGroup == null) {
            return false;
        }
        return getCompatibleDonorGroups(recipientGroup).contains(donorGroup);
    }
}
