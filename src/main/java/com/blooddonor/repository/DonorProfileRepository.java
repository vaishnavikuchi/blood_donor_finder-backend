package com.blooddonor.repository;

import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.enums.BloodGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {
    Optional<DonorProfile> findByUserId(Long userId);
    boolean existsByUserId(Long userId);

    @Query("SELECT dp FROM DonorProfile dp JOIN FETCH dp.user u WHERE " +
           "(:bloodGroups IS NULL OR dp.bloodGroup IN :bloodGroups) AND " +
           "(:city IS NULL OR LOWER(dp.city) = LOWER(:city)) AND " +
           "(:availableOnly IS NULL OR :availableOnly = false OR dp.available = true)")
    List<DonorProfile> searchDonors(
        @Param("bloodGroups") List<BloodGroup> bloodGroups,
        @Param("city") String city,
        @Param("availableOnly") Boolean availableOnly
    );

    @Query("SELECT dp FROM DonorProfile dp JOIN FETCH dp.user u")
    List<DonorProfile> findAllWithUser();

    long countByBloodGroup(BloodGroup bloodGroup);
}
