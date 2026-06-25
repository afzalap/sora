package com.sora.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateBookingRequest(
        String carrier,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        String currency
) {}
