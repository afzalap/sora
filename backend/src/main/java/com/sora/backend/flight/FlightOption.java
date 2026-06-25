package com.sora.backend.flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightOption(
        String carrier,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        int durationMinutes,
        int stops,
        BigDecimal price,
        String currency
) {}
