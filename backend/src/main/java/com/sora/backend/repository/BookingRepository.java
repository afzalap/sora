package com.sora.backend.repository;

import com.sora.backend.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUsername(String username);

    Optional<Booking> findByIdAndUsername(Long id, String username);
}
