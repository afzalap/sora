package com.sora.backend.flight;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/**
 * Hard-coded flights for development and demo purposes.
 * Replace this bean with SerpApiFlightSearchProvider (Phase 6+) and this one
 * can be deleted or kept behind a @Profile("dev") annotation.
 */
@Component
public class SeededFlightSearchProvider implements FlightSearchProvider {

    // ── Single-date search ────────────────────────────────────────────────────

    @Override
    public List<FlightOption> search(String origin, String destination, LocalDate departureDate) {
        if ("HND".equalsIgnoreCase(origin) && "KMJ".equalsIgnoreCase(destination)) {
            return hndToKmj(departureDate);
        }
        return genericOneWay(origin, destination, departureDate);
    }

    // ── Flexible-date cheapest round-trip search ──────────────────────────────

    @Override
    public List<FlightOption> searchCheapest(String origin, String destination,
                                              String yearMonth, int tripDurationDays) {
        YearMonth ym = YearMonth.parse(yearMonth);

        if ("HND".equalsIgnoreCase(origin) && "KMJ".equalsIgnoreCase(destination)) {
            return hndToKmjCheapest(ym, tripDurationDays);
        }
        return genericCheapest(origin, destination, ym, tripDurationDays);
    }

    // ── HND → KMJ seed data ───────────────────────────────────────────────────

    private List<FlightOption> hndToKmj(LocalDate date) {
        return List.of(
                option("ANA", "NH645", "HND", "KMJ", date, LocalTime.of(7, 0),   130, 0, "18500", "JPY"),
                option("JAL", "JL655", "HND", "KMJ", date, LocalTime.of(10, 30), 130, 0, "16200", "JPY"),
                option("ANA", "NH647", "HND", "KMJ", date, LocalTime.of(16, 0),  130, 0, "15800", "JPY")
        );
    }

    /**
     * Simulates a flexible-date cheapest round-trip search for HND → KMJ.
     * Three departure dates spread across the month, round-trip prices, sorted cheapest first.
     * In production SerpApi returns this natively via its flexible-dates endpoint.
     */
    private List<FlightOption> hndToKmjCheapest(YearMonth ym, int tripDurationDays) {
        // Spread across early / mid / late month so the demo feels realistic
        LocalDate early = ym.atDay(3);
        LocalDate mid   = ym.atDay(10);
        LocalDate late  = ym.atDay(20);

        return List.of(
                // Cheapest: late-month ANA departure, round-trip ¥26,800
                roundTrip("ANA", "NH647", "HND", "KMJ", late, LocalTime.of(16, 0),
                        130, tripDurationDays, "26800", "JPY"),
                // Mid: early-month JAL, round-trip ¥31,200
                roundTrip("JAL", "JL655", "HND", "KMJ", early, LocalTime.of(10, 30),
                        130, tripDurationDays, "31200", "JPY"),
                // Priciest: mid-month ANA morning, round-trip ¥34,500
                roundTrip("ANA", "NH645", "HND", "KMJ", mid, LocalTime.of(7, 0),
                        130, tripDurationDays, "34500", "JPY")
        );
    }

    // ── Generic fallback data ─────────────────────────────────────────────────

    private List<FlightOption> genericOneWay(String origin, String destination, LocalDate date) {
        return List.of(
                option("ANA", "NH001", origin, destination, date, LocalTime.of(8, 0),   120, 0, "32000", "JPY"),
                option("JAL", "JL002", origin, destination, date, LocalTime.of(14, 30), 120, 0, "29500", "JPY")
        );
    }

    private List<FlightOption> genericCheapest(String origin, String destination,
                                                YearMonth ym, int tripDurationDays) {
        return List.of(
                roundTrip("JAL", "JL002", origin, destination, ym.atDay(5),
                        LocalTime.of(9, 0), 120, tripDurationDays, "48000", "JPY"),
                roundTrip("ANA", "NH001", origin, destination, ym.atDay(15),
                        LocalTime.of(13, 0), 120, tripDurationDays, "52000", "JPY")
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** One-way flight option. */
    private FlightOption option(String carrier, String flightNumber,
                                String origin, String destination,
                                LocalDate date, LocalTime departure,
                                int durationMinutes, int stops,
                                String price, String currency) {
        LocalDateTime dep = LocalDateTime.of(date, departure);
        LocalDateTime arr = dep.plusMinutes(durationMinutes);
        return new FlightOption(carrier, flightNumber, origin, destination,
                dep, arr, durationMinutes, stops, new BigDecimal(price), currency);
    }

    /**
     * Round-trip option. The price covers both legs.
     * returnDate = departureDate + tripDurationDays (the LLM is told this in the tool description).
     */
    private FlightOption roundTrip(String carrier, String flightNumber,
                                   String origin, String destination,
                                   LocalDate outboundDate, LocalTime departure,
                                   int durationMinutes, int tripDurationDays,
                                   String roundTripPrice, String currency) {
        LocalDateTime dep = LocalDateTime.of(outboundDate, departure);
        LocalDateTime arr = dep.plusMinutes(durationMinutes);
        // arrivalTime on the record = outbound arrival; return date is outboundDate + tripDurationDays
        return new FlightOption(carrier, flightNumber, origin, destination,
                dep, arr, durationMinutes, 0, new BigDecimal(roundTripPrice), currency);
    }
}
