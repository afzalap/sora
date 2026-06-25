package com.sora.backend.service;

import com.sora.backend.flight.FlightSearchProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are Sora, a concise flight-booking AI assistant. Today is %s.

            RULES — follow these exactly:
            1. For specific-date searches, call searchFlights. \
            For "cheapest in [month]" or flexible-date requests, call searchCheapestFlights.
            2. When the user selects a flight, summarise it (airline, flight number, route, times, price) \
            and ask for explicit confirmation before booking.
            3. Only call createBooking after the user says "yes", "confirm", or words to that effect.
            4. When the user asks to cancel a booking, call getMyBookings to find it, state which booking \
            you will cancel, and ask for confirmation before calling cancelBooking.
            5. Keep responses short and action-oriented. Use IATA codes for airports.
            6. Politely decline requests for hotels, rental cars, multi-city itineraries, or multi-passenger \
            bookings — explain that Sora handles single-passenger flights only.
            """;

    private final ChatClient chatClient;
    private final FlightSearchProvider flightSearchProvider;
    private final BookingService bookingService;

    public ChatService(ChatClient.Builder chatClientBuilder,
                       FlightSearchProvider flightSearchProvider,
                       BookingService bookingService) {
        this.chatClient = chatClientBuilder.build();
        this.flightSearchProvider = flightSearchProvider;
        this.bookingService = bookingService;
    }

    public String chat(String message, String username) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT.formatted(LocalDate.now()))
                .user(message)
                .tools(new SoraTools(username, flightSearchProvider, bookingService))
                .call()
                .content();
    }

    // SSE streaming variant — used by ChatController's /api/chat/stream endpoint
    public Flux<String> stream(String message, String username) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT.formatted(LocalDate.now()))
                .user(message)
                .tools(new SoraTools(username, flightSearchProvider, bookingService))
                .stream()
                .content();
    }
}
