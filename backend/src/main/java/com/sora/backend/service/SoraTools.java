package com.sora.backend.service;

import com.sora.backend.dto.BookingResponse;
import com.sora.backend.dto.CreateBookingRequest;
import com.sora.backend.flight.FlightOption;
import com.sora.backend.flight.FlightSearchProvider;
import org.springframework.ai.tool.annotation.Tool;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Plain class (not a Spring bean) — instantiated per chat request with the
 * authenticated username baked in. Spring AI discovers the @Tool methods via
 * reflection when this object is passed to ChatClient.prompt().tools(...).
 */
public class SoraTools {

    private final String username;
    private final FlightSearchProvider flightSearchProvider;
    private final BookingService bookingService;

    public SoraTools(String username, FlightSearchProvider flightSearchProvider, BookingService bookingService) {
        this.username = username;
        this.flightSearchProvider = flightSearchProvider;
        this.bookingService = bookingService;
    }

    @Tool(description = "Get today's date in ISO format (YYYY-MM-DD). Call this when you need to know the current date.")
    public String getToday() {
        return LocalDate.now().toString();
    }

    @Tool(description = """
            Search for available flights between two airports on a given departure date.
            Origin and destination must be IATA airport codes (e.g. HND, KMJ, NRT).
            Departure date must be in ISO format YYYY-MM-DD.
            """)
    public List<FlightOption> searchFlights(String origin, String destination, String departureDate) {
        return flightSearchProvider.search(origin, destination, LocalDate.parse(departureDate));
    }

    @Tool(description = """
            Search for the cheapest round-trip flights within a given month.
            Use this when the user asks for cheapest flights over a flexible date range or mentions a month rather than a specific date.
            yearMonth must be in YYYY-MM format (e.g. "2026-08" for August 2026).
            tripDurationDays is the length of the round trip in days (e.g. 15).
            Results are sorted cheapest first. Prices shown are round-trip totals covering both outbound and return legs.
            The return date for each option is departureDate + tripDurationDays.
            """)
    public List<FlightOption> searchCheapestFlights(String origin, String destination,
                                                     String yearMonth, int tripDurationDays) {
        return flightSearchProvider.searchCheapest(origin, destination, yearMonth, tripDurationDays);
    }

    @Tool(description = "Get the current user's flight bookings (confirmed and cancelled).")
    public List<BookingResponse> getMyBookings() {
        return bookingService.getBookingsForUser(username);
    }

    @Tool(description = """
            Create a confirmed booking for the current user.
            Only call this after the user has explicitly confirmed they want to book.
            All date-time values must be in ISO format (YYYY-MM-DDTHH:MM:SS).
            Price must be a plain number with no currency symbol.
            """)
    public BookingResponse createBooking(
            String carrier,
            String flightNumber,
            String origin,
            String destination,
            String departureTime,
            String arrivalTime,
            String price,
            String currency
    ) {
        CreateBookingRequest req = new CreateBookingRequest(
                carrier, flightNumber, origin, destination,
                LocalDateTime.parse(departureTime),
                LocalDateTime.parse(arrivalTime),
                new BigDecimal(price),
                currency
        );
        return bookingService.createBooking(req, username);
    }

    @Tool(description = "Cancel one of the current user's bookings by its numeric ID.")
    public BookingResponse cancelBooking(Long bookingId) {
        return bookingService.cancelBooking(bookingId, username);
    }
}
