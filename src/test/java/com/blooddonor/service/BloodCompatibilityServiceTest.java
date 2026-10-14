package com.blooddonor.service;

import com.blooddonor.entity.enums.BloodGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Blood Compatibility Service Tests")
class BloodCompatibilityServiceTest {

    private BloodCompatibilityService compatibilityService;

    @BeforeEach
    void setUp() {
        compatibilityService = new BloodCompatibilityService();
    }

    @Test
    @DisplayName("Recipient O- can only receive from O-")
    void testONegativeRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.O_NEG);
        assertEquals(1, donors.size());
        assertTrue(donors.contains(BloodGroup.O_NEG));
        assertFalse(compatibilityService.isCompatible(BloodGroup.O_NEG, BloodGroup.O_POS));
        assertFalse(compatibilityService.isCompatible(BloodGroup.O_NEG, BloodGroup.A_NEG));
    }

    @Test
    @DisplayName("Recipient O+ can receive from O-, O+")
    void testOPositiveRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.O_POS);
        assertEquals(Set.of(BloodGroup.O_NEG, BloodGroup.O_POS), donors);
        assertTrue(compatibilityService.isCompatible(BloodGroup.O_POS, BloodGroup.O_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.O_POS, BloodGroup.O_POS));
        assertFalse(compatibilityService.isCompatible(BloodGroup.O_POS, BloodGroup.A_POS));
    }

    @Test
    @DisplayName("Recipient A- can receive from O-, A-")
    void testANegativeRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.A_NEG);
        assertEquals(Set.of(BloodGroup.O_NEG, BloodGroup.A_NEG), donors);
        assertTrue(compatibilityService.isCompatible(BloodGroup.A_NEG, BloodGroup.O_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.A_NEG, BloodGroup.A_NEG));
        assertFalse(compatibilityService.isCompatible(BloodGroup.A_NEG, BloodGroup.A_POS));
    }

    @Test
    @DisplayName("Recipient A+ can receive from O-, O+, A-, A+")
    void testAPositiveRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.A_POS);
        assertEquals(Set.of(BloodGroup.O_NEG, BloodGroup.O_POS, BloodGroup.A_NEG, BloodGroup.A_POS), donors);
        assertTrue(compatibilityService.isCompatible(BloodGroup.A_POS, BloodGroup.O_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.A_POS, BloodGroup.O_POS));
        assertTrue(compatibilityService.isCompatible(BloodGroup.A_POS, BloodGroup.A_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.A_POS, BloodGroup.A_POS));
        assertFalse(compatibilityService.isCompatible(BloodGroup.A_POS, BloodGroup.B_POS));
    }

    @Test
    @DisplayName("Recipient B- can receive from O-, B-")
    void testBNegativeRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.B_NEG);
        assertEquals(Set.of(BloodGroup.O_NEG, BloodGroup.B_NEG), donors);
        assertTrue(compatibilityService.isCompatible(BloodGroup.B_NEG, BloodGroup.O_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.B_NEG, BloodGroup.B_NEG));
        assertFalse(compatibilityService.isCompatible(BloodGroup.B_NEG, BloodGroup.B_POS));
    }

    @Test
    @DisplayName("Recipient B+ can receive from O-, O+, B-, B+")
    void testBPositiveRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.B_POS);
        assertEquals(Set.of(BloodGroup.O_NEG, BloodGroup.O_POS, BloodGroup.B_NEG, BloodGroup.B_POS), donors);
        assertTrue(compatibilityService.isCompatible(BloodGroup.B_POS, BloodGroup.O_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.B_POS, BloodGroup.O_POS));
        assertTrue(compatibilityService.isCompatible(BloodGroup.B_POS, BloodGroup.B_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.B_POS, BloodGroup.B_POS));
        assertFalse(compatibilityService.isCompatible(BloodGroup.B_POS, BloodGroup.A_POS));
    }

    @Test
    @DisplayName("Recipient AB- can receive from O-, A-, B-, AB-")
    void testABNegativeRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.AB_NEG);
        assertEquals(Set.of(BloodGroup.O_NEG, BloodGroup.A_NEG, BloodGroup.B_NEG, BloodGroup.AB_NEG), donors);
        assertTrue(compatibilityService.isCompatible(BloodGroup.AB_NEG, BloodGroup.O_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.AB_NEG, BloodGroup.A_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.AB_NEG, BloodGroup.B_NEG));
        assertTrue(compatibilityService.isCompatible(BloodGroup.AB_NEG, BloodGroup.AB_NEG));
        assertFalse(compatibilityService.isCompatible(BloodGroup.AB_NEG, BloodGroup.AB_POS));
    }

    @Test
    @DisplayName("Recipient AB+ can receive from all 8 blood groups (Universal Recipient)")
    void testABPositiveRecipient() {
        Set<BloodGroup> donors = compatibilityService.getCompatibleDonorGroups(BloodGroup.AB_POS);
        assertEquals(8, donors.size());
        for (BloodGroup bg : BloodGroup.values()) {
            assertTrue(donors.contains(bg));
            assertTrue(compatibilityService.isCompatible(BloodGroup.AB_POS, bg));
        }
    }
}
