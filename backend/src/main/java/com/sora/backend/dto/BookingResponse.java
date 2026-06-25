package com.sora.backend.dto;

import com.sora.backend.domain.Booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String bookingReference,
        String carrier,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        String currency,
        String status,
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getCarrier(),
                booking.getFlightNumber(),
                booking.getOrigin(),
                booking.getDestination(),
                booking.getDepartureTime(),
                booking.getArrivalTime(),
                booking.getPrice(),
                booking.getCurrency(),
                booking.getStatus().name(),
                booking.getCreatedAt()
        );
    }
}
