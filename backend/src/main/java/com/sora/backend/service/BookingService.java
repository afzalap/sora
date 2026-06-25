package com.sora.backend.service;

import com.sora.backend.domain.Booking;
import com.sora.backend.dto.BookingResponse;
import com.sora.backend.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<BookingResponse> getBookingsForUser(String username) {
        return bookingRepository.findByUsername(username)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String username) {
        Booking booking = bookingRepository.findByIdAndUsername(bookingId, username)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking " + bookingId + " is already cancelled");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(java.time.LocalDateTime.now());

        return BookingResponse.from(bookingRepository.save(booking));
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(Long id) {
            super("Booking " + id + " not found");
        }
    }
}
