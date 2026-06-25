package com.sora.backend.service;

import com.sora.backend.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void userCannotCancelAnotherUsersBooking() {
        // Booking 1 belongs to alice. Bob queries with his own username,
        // so findByIdAndUsername returns empty — as if the booking doesn't exist for him.
        when(bookingRepository.findByIdAndUsername(1L, "bob"))
                .thenReturn(Optional.empty());

        assertThrows(
                BookingService.BookingNotFoundException.class,
                () -> bookingService.cancelBooking(1L, "bob")
        );
    }
}
