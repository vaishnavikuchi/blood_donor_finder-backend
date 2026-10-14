package com.blooddonor.runner;

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
import com.blooddonor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorResponseRepository donorResponseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // If old sample data with "john@example.com" exists, clear it to upgrade to Indian dataset
        if (userRepository.existsByEmail("john@example.com")) {
            log.info("Old dataset detected. Upgrading to Indian dataset...");
            donorResponseRepository.deleteAll();
            bloodRequestRepository.deleteAll();
            donorProfileRepository.deleteAll();
            userRepository.deleteAll();
        } else if (userRepository.count() > 0) {
            log.info("Database already seeded with Indian dataset. Skipping.");
            return;
        }

        log.info("Seeding Indian dataset for Blood Donor Finder (BloodLink India)...");
        String encodedPassword = passwordEncoder.encode("password123");

        // 4 Primary Requester Users in India
        User user1 = createUser("Rahul Sharma", "rahul.sharma@example.com", "+91 98765 43210", encodedPassword);
        User user2 = createUser("Priya Patel", "priya.patel@example.com", "+91 98230 11223", encodedPassword);
        User user3 = createUser("Ananya Reddy", "ananya.reddy@example.com", "+91 99490 33445", encodedPassword);
        User user4 = createUser("Karthik Nair", "karthik.nair@example.com", "+91 97450 55667", encodedPassword);

        // 12 Donors across major Indian cities (Hyderabad, Bengaluru, Mumbai, Delhi, Chennai, Kolkata, Pune)
        User dUser1 = createUser("Aarav Kumar", "aarav.a_pos@example.com", "+91 98111 22334", encodedPassword);
        User dUser2 = createUser("Sneha Rao", "sneha.a_neg@example.com", "+91 98480 33445", encodedPassword);
        User dUser3 = createUser("Vikram Joshi", "vikram.b_pos@example.com", "+91 98200 44556", encodedPassword);
        User dUser4 = createUser("Deepa Sundaram", "deepa.b_neg@example.com", "+91 94440 55667", encodedPassword);
        User dUser5 = createUser("Rohan Gupta", "rohan.ab_pos@example.com", "+91 98100 66778", encodedPassword);
        User dUser6 = createUser("Pooja Verma", "pooja.ab_neg@example.com", "+91 98300 77889", encodedPassword);
        User dUser7 = createUser("Suresh Naidu", "suresh.o_pos@example.com", "+91 98490 88990", encodedPassword);
        User dUser8 = createUser("Divya Mehra", "divya.o_neg@example.com", "+91 98190 99001", encodedPassword);
        User dUser9 = createUser("Arvind Swaminathan", "arvind.o_neg@example.com", "+91 98400 11223", encodedPassword);
        User dUser10 = createUser("Neha Deshmukh", "neha.a_pos@example.com", "+91 98220 22334", encodedPassword);
        User dUser11 = createUser("Manoj Pillai", "manoj.b_pos@example.com", "+91 98470 33445", encodedPassword);
        User dUser12 = createUser("Kavita Chawla", "kavita.o_pos@example.com", "+91 98110 44556", encodedPassword);

        // 12 Donor Profiles across all 8 blood groups
        // Hyderabad
        DonorProfile dp1 = createDonor(dUser1, BloodGroup.A_POS, "Hyderabad", LocalDate.of(1996, 4, 15), 72, null, true);
        // Bengaluru
        DonorProfile dp2 = createDonor(dUser2, BloodGroup.A_NEG, "Bengaluru", LocalDate.of(1993, 8, 22), 58, LocalDate.now().minusDays(115), true);
        // Mumbai (Recently donated - 30 days cooldown!)
        DonorProfile dp3 = createDonor(dUser3, BloodGroup.B_POS, "Mumbai", LocalDate.of(1990, 11, 5), 80, LocalDate.now().minusDays(30), true);
        // Chennai
        DonorProfile dp4 = createDonor(dUser4, BloodGroup.B_NEG, "Chennai", LocalDate.of(1998, 2, 14), 62, null, true);
        // Delhi
        DonorProfile dp5 = createDonor(dUser5, BloodGroup.AB_POS, "Delhi", LocalDate.of(1992, 9, 18), 85, LocalDate.now().minusDays(100), true);
        // Kolkata
        DonorProfile dp6 = createDonor(dUser6, BloodGroup.AB_NEG, "Kolkata", LocalDate.of(1995, 6, 30), 65, LocalDate.now().minusDays(140), true);
        // Hyderabad (45 days cooldown)
        DonorProfile dp7 = createDonor(dUser7, BloodGroup.O_POS, "Hyderabad", LocalDate.of(1989, 3, 12), 76, LocalDate.now().minusDays(45), true);
        // Mumbai (Universal donor O-)
        DonorProfile dp8 = createDonor(dUser8, BloodGroup.O_NEG, "Mumbai", LocalDate.of(1997, 10, 25), 64, LocalDate.now().minusDays(95), true);
        // Chennai (O- unavailable)
        DonorProfile dp9 = createDonor(dUser9, BloodGroup.O_NEG, "Chennai", LocalDate.of(1987, 5, 8), 78, null, false);
        // Pune
        DonorProfile dp10 = createDonor(dUser10, BloodGroup.A_POS, "Pune", LocalDate.of(1994, 7, 19), 70, null, true);
        // Kochi
        DonorProfile dp11 = createDonor(dUser11, BloodGroup.B_POS, "Kochi", LocalDate.of(1996, 12, 1), 68, LocalDate.now().minusDays(120), true);
        // Delhi
        DonorProfile dp12 = createDonor(dUser12, BloodGroup.O_POS, "Delhi", LocalDate.of(1993, 1, 10), 66, LocalDate.now().minusDays(180), true);

        // 5 Sample Blood Requests with Indian Hospitals & Urgencies
        // 1. Critical in Hyderabad
        BloodRequest req1 = BloodRequest.builder()
                .requester(user1)
                .patientName("Rajesh Kulkarni")
                .bloodGroup(BloodGroup.O_NEG)
                .unitsNeeded(3)
                .unitsFulfilled(1)
                .hospital("Apollo Hospitals, Jubilee Hills")
                .city("Hyderabad")
                .urgency(Urgency.CRITICAL)
                .requiredBy(LocalDateTime.now().plusHours(8))
                .contactPhone("+91 98765 43210")
                .note("Emergency cardiac surgery in OT-3. Immediate O- units required.")
                .status(RequestStatus.OPEN)
                .build();
        bloodRequestRepository.save(req1);

        // 2. Critical in Bengaluru
        BloodRequest req2 = BloodRequest.builder()
                .requester(user2)
                .patientName("Meera Nambiar")
                .bloodGroup(BloodGroup.A_POS)
                .unitsNeeded(2)
                .unitsFulfilled(0)
                .hospital("Manipal Hospital, Old Airport Road")
                .city("Bengaluru")
                .urgency(Urgency.CRITICAL)
                .requiredBy(LocalDateTime.now().plusHours(12))
                .contactPhone("+91 98230 11223")
                .note("Urgent requirement for platelet & RBC transfusion in ICU.")
                .status(RequestStatus.OPEN)
                .build();
        bloodRequestRepository.save(req2);

        // 3. Urgent in Mumbai
        BloodRequest req3 = BloodRequest.builder()
                .requester(user3)
                .patientName("Amitabh Saxena")
                .bloodGroup(BloodGroup.B_POS)
                .unitsNeeded(4)
                .unitsFulfilled(1)
                .hospital("Tata Memorial Hospital, Parel")
                .city("Mumbai")
                .urgency(Urgency.URGENT)
                .requiredBy(LocalDateTime.now().plusDays(1))
                .contactPhone("+91 99490 33445")
                .note("Chemotherapy support and replacement transfusion.")
                .status(RequestStatus.OPEN)
                .build();
        bloodRequestRepository.save(req3);

        // 4. Urgent in Delhi
        BloodRequest req4 = BloodRequest.builder()
                .requester(user4)
                .patientName("Gurpreet Singh")
                .bloodGroup(BloodGroup.AB_POS)
                .unitsNeeded(2)
                .unitsFulfilled(0)
                .hospital("AIIMS Hospital, Ansari Nagar")
                .city("Delhi")
                .urgency(Urgency.URGENT)
                .requiredBy(LocalDateTime.now().plusDays(2))
                .contactPhone("+91 97450 55667")
                .note("Post-accident trauma stabilization.")
                .status(RequestStatus.OPEN)
                .build();
        bloodRequestRepository.save(req4);

        // 5. Normal in Chennai
        BloodRequest req5 = BloodRequest.builder()
                .requester(user1)
                .patientName("S. Ranganathan")
                .bloodGroup(BloodGroup.O_POS)
                .unitsNeeded(2)
                .unitsFulfilled(0)
                .hospital("Fortis Malar Hospital, Adyar")
                .city("Chennai")
                .urgency(Urgency.NORMAL)
                .requiredBy(LocalDateTime.now().plusDays(4))
                .contactPhone("+91 98765 43210")
                .note("Scheduled hip replacement surgery next week.")
                .status(RequestStatus.OPEN)
                .build();
        bloodRequestRepository.save(req5);

        // Seed Sample Offers
        // Divya Mehra (O-) offers on Rajesh Kulkarni's request in Hyderabad
        DonorResponse offer1 = DonorResponse.builder()
                .request(req1)
                .donor(dp8)
                .status(OfferStatus.CONFIRMED)
                .build();
        donorResponseRepository.save(offer1);

        // Sneha Rao (A-) offers on Meera Nambiar's request in Bengaluru
        DonorResponse offer2 = DonorResponse.builder()
                .request(req2)
                .donor(dp2)
                .status(OfferStatus.OFFERED)
                .build();
        donorResponseRepository.save(offer2);

        // Deepa Sundaram (B-) offers on Amitabh Saxena's request in Mumbai
        DonorResponse offer3 = DonorResponse.builder()
                .request(req3)
                .donor(dp4)
                .status(OfferStatus.OFFERED)
                .build();
        donorResponseRepository.save(offer3);

        log.info("Successfully seeded Indian dataset: 16 users, 12 donors across Indian metros, 5 emergency requests, and 3 active offers.");
    }

    private User createUser(String name, String email, String phone, String password) {
        User user = User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .password(password)
                .build();
        return userRepository.save(user);
    }

    private DonorProfile createDonor(User user, BloodGroup bg, String city, LocalDate dob, int weight, LocalDate lastDonated, boolean available) {
        DonorProfile profile = DonorProfile.builder()
                .user(user)
                .bloodGroup(bg)
                .city(city)
                .dateOfBirth(dob)
                .weightKg(weight)
                .lastDonationDate(lastDonated)
                .available(available)
                .build();
        return donorProfileRepository.save(profile);
    }
}
