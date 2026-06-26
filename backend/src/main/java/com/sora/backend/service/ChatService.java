package com.sora.backend.service;

import com.sora.backend.dto.ChatResponse;
import com.sora.backend.flight.FlightSearchProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ChatService {

    private static final String SYSTEM_PROMPT = """
            You are Sora, a concise flight-booking AI assistant. Today is %s.

            RULES — follow these exactly:
            1. For specific-date searches, call searchFlights. \
            For "cheapest in [month]" or flexible-date requests, call searchCheapestFlights.
            2. After a flight search, write ONE short sentence (e.g. "Here are the flights I found:"). \
            Do NOT list flight details, prices, times, or airline names in text — the UI renders cards for that.
            3. When the user selects a flight, summarise it (airline, flight number, route, times, price) \
            and ask for explicit confirmation before booking.
            4. Only call createBooking after the user says "yes", "confirm", or words to that effect.
            5. When the user asks to cancel a booking, call getMyBookings to find it, state which booking \
            you will cancel, and ask for confirmation before calling cancelBooking.
            6. Keep responses short and action-oriented. Use IATA codes for airports.
            7. Politely decline requests for hotels, rental cars, multi-city itineraries, or multi-passenger \
            bookings — explain that Sora handles single-passenger flights only.
            """;

    private final ChatClient chatClient;
    private final FlightSearchProvider flightSearchProvider;
    private final BookingService bookingService;

    // Storage layer: InMemoryChatMemoryRepository for Phase 1.
    // Phase 2: swap for JdbcChatMemoryRepository — nothing else in this class changes.
    private final ChatMemory chatMemory = MessageWindowChatMemory.builder()
            .chatMemoryRepository(new InMemoryChatMemoryRepository())
            .build();

    public ChatService(ChatClient.Builder chatClientBuilder,
                       FlightSearchProvider flightSearchProvider,
                       BookingService bookingService) {
        this.chatClient = chatClientBuilder.build();
        this.flightSearchProvider = flightSearchProvider;
        this.bookingService = bookingService;
    }

    public ChatResponse chat(String message, String username) {
        var tools = new SoraTools(username, flightSearchProvider, bookingService);

        String reply = chatClient.prompt()
                .system(SYSTEM_PROMPT.formatted(LocalDate.now()))
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, username))
                .user(message)
                .tools(tools)
                .call()
                .content();

        return new ChatResponse(reply, tools.getLastFlightResults());
    }
}
