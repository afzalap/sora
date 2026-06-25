package com.sora.backend.controller;

import com.sora.backend.dto.BookingResponse;
import com.sora.backend.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponse> getMyBookings(Principal principal) {
        return bookingService.getBookingsForUser(principal.getName());
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long id,
            Principal principal
    ) {
        BookingResponse response = bookingService.cancelBooking(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(BookingService.BookingNotFoundException.class)
    public ResponseEntity<String> handleNotFound(BookingService.BookingNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleBadState(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
