package com.blooddonor.repository;

import com.blooddonor.entity.DonorResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorResponseRepository extends JpaRepository<DonorResponse, Long> {

    @Query("SELECT dr FROM DonorResponse dr " +
           "JOIN FETCH dr.donor d " +
           "JOIN FETCH d.user u " +
           "JOIN FETCH dr.request r " +
           "WHERE dr.request.id = :requestId ORDER BY dr.createdAt DESC")
    List<DonorResponse> findByRequestIdWithDetails(@Param("requestId") Long requestId);

    @Query("SELECT dr FROM DonorResponse dr " +
           "JOIN FETCH dr.donor d " +
           "JOIN FETCH dr.request r " +
           "JOIN FETCH r.requester req " +
           "WHERE d.user.id = :userId ORDER BY dr.createdAt DESC")
    List<DonorResponse> findByDonorUserIdWithDetails(@Param("userId") Long userId);

    boolean existsByRequestIdAndDonorId(Long requestId, Long donorId);

    Optional<DonorResponse> findByRequestIdAndDonorId(Long requestId, Long donorId);
}
