package com.blooddonor.service;

import com.blooddonor.dto.DonorResponseDto;
import com.blooddonor.entity.BloodRequest;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.DonorResponse;
import com.blooddonor.entity.User;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.OfferStatus;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.entity.enums.Urgency;
import com.blooddonor.repository.BloodRequestRepository;
import com.blooddonor.repository.DonorProfileRepository;
import com.blooddonor.repository.DonorResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Donor Response & Auto-Fulfillment Tests")
class DonorResponseServiceTest {

    @Mock
    private DonorResponseRepository donorResponseRepository;
    @Mock
    private BloodRequestRepository bloodRequestRepository;
    @Mock
    private DonorProfileRepository donorProfileRepository;
    @Mock
    private BloodCompatibilityService compatibilityService;
    @Mock
    private DonorEligibilityService eligibilityService;

    @InjectMocks
    private DonorResponseService donorResponseService;

    private User requester;
    private User donorUser;
    private DonorProfile donorProfile;
    private BloodRequest request;
    private DonorResponse offer;

    @BeforeEach
    void setUp() {
        requester = User.builder().id(1L).name("Requester").email("requester@test.com").phone("123").build();
        donorUser = User.builder().id(2L).name("Donor").email("donor@test.com").phone("456").build();

        donorProfile = DonorProfile.builder()
                .id(10L)
                .user(donorUser)
                .bloodGroup(BloodGroup.O_NEG)
                .city("New York")
                .dateOfBirth(LocalDate.now().minusYears(25))
                .weightKg(70)
                .available(true)
                .lastDonationDate(null)
                .build();

        request = BloodRequest.builder()
                .id(100L)
                .requester(requester)
                .patientName("Patient A")
                .bloodGroup(BloodGroup.O_NEG)
                .unitsNeeded(2)
                .unitsFulfilled(1) // 1 fulfilled, need 1 more
                .hospital("City Hospital")
                .city("New York")
                .urgency(Urgency.CRITICAL)
                .requiredBy(LocalDateTime.now().plusDays(1))
                .contactPhone("999")
                .status(RequestStatus.OPEN)
                .build();

        offer = DonorResponse.builder()
                .id(50L)
                .request(request)
                .donor(donorProfile)
                .status(OfferStatus.CONFIRMED)
                .build();
    }

    @Test
    @DisplayName("When offer is marked DONATED and units_fulfilled reaches units_needed, request becomes FULFILLED")
    void testAutoFulfillmentWhenUnitsNeededReached() {
        when(donorResponseRepository.findById(50L)).thenReturn(Optional.of(offer));
        when(donorResponseRepository.save(any(DonorResponse.class))).thenAnswer(i -> i.getArgument(0));

        DonorResponseDto result = donorResponseService.updateOfferStatus(50L, requester, OfferStatus.DONATED);

        assertNotNull(result);
        assertEquals(OfferStatus.DONATED, offer.getStatus());
        assertEquals(2, request.getUnitsFulfilled());
        assertEquals(RequestStatus.FULFILLED, request.getStatus());
        assertEquals(LocalDate.now(), donorProfile.getLastDonationDate());

        verify(donorProfileRepository, times(1)).save(donorProfile);
        verify(bloodRequestRepository, times(1)).save(request);
        verify(donorResponseRepository, times(1)).save(offer);
    }

    @Test
    @DisplayName("When offer is marked DONATED but units_fulfilled is less than units_needed, request stays OPEN")
    void testRequestRemainsOpenWhenMoreUnitsNeeded() {
        request.setUnitsNeeded(3);
        request.setUnitsFulfilled(1); // will become 2, which is < 3

        when(donorResponseRepository.findById(50L)).thenReturn(Optional.of(offer));
        when(donorResponseRepository.save(any(DonorResponse.class))).thenAnswer(i -> i.getArgument(0));

        DonorResponseDto result = donorResponseService.updateOfferStatus(50L, requester, OfferStatus.DONATED);

        assertNotNull(result);
        assertEquals(2, request.getUnitsFulfilled());
        assertEquals(RequestStatus.OPEN, request.getStatus());
        assertEquals(LocalDate.now(), donorProfile.getLastDonationDate());

        verify(donorProfileRepository, times(1)).save(donorProfile);
        verify(bloodRequestRepository, times(1)).save(request);
    }
}
