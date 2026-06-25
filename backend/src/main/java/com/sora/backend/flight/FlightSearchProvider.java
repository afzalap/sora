package com.sora.backend.flight;

import java.time.LocalDate;
import java.util.List;

public interface FlightSearchProvider {

    // Single-date search — "flights HND→KMJ on Aug 3"
    List<FlightOption> search(String origin, String destination, LocalDate departureDate);

    // Flexible-date search — "cheapest round trip HND→KMJ in August for 15 days"
    // yearMonth format: "YYYY-MM". Prices returned are round-trip totals.
    List<FlightOption> searchCheapest(String origin, String destination,
                                      String yearMonth, int tripDurationDays);
}
