package com.blooddonor.repository;

import com.blooddonor.entity.BloodRequest;
import com.blooddonor.entity.enums.BloodGroup;
import com.blooddonor.entity.enums.RequestStatus;
import com.blooddonor.entity.enums.Urgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    @Query("SELECT r FROM BloodRequest r JOIN FETCH r.requester WHERE " +
           "(:bloodGroup IS NULL OR r.bloodGroup = :bloodGroup) AND " +
           "(:city IS NULL OR LOWER(r.city) = LOWER(:city)) AND " +
           "(:urgency IS NULL OR r.urgency = :urgency) AND " +
           "(:status IS NULL OR r.status = :status) " +
           "ORDER BY " +
           "CASE r.urgency " +
           "  WHEN com.blooddonor.entity.enums.Urgency.CRITICAL THEN 1 " +
           "  WHEN com.blooddonor.entity.enums.Urgency.URGENT THEN 2 " +
           "  WHEN com.blooddonor.entity.enums.Urgency.NORMAL THEN 3 " +
           "  ELSE 4 " +
           "END ASC, " +
           "r.requiredBy ASC")
    List<BloodRequest> filterRequests(
        @Param("bloodGroup") BloodGroup bloodGroup,
        @Param("city") String city,
        @Param("urgency") Urgency urgency,
        @Param("status") RequestStatus status
    );

    @Query("SELECT r FROM BloodRequest r JOIN FETCH r.requester WHERE r.requester.id = :requesterId ORDER BY r.createdAt DESC")
    List<BloodRequest> findByRequesterIdOrderByCreatedAtDesc(@Param("requesterId") Long requesterId);

    List<BloodRequest> findByStatusAndRequiredByBefore(RequestStatus status, LocalDateTime now);

    long countByStatus(RequestStatus status);

    @Query("SELECT COALESCE(SUM(r.unitsFulfilled), 0) FROM BloodRequest r")
    long sumTotalUnitsDonated();
}
