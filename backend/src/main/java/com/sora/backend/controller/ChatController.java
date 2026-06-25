package com.sora.backend.controller;

import com.sora.backend.dto.ChatRequest;
import com.sora.backend.dto.ChatResponse;
import com.sora.backend.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Non-streaming: returns the full reply once the model is done
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request, Principal principal) {
        String reply = chatService.chat(request.message(), principal.getName());
        return new ChatResponse(reply);
    }

    // SSE streaming: sends tokens to the client as they arrive
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestBody ChatRequest request, Principal principal) {
        return chatService.stream(request.message(), principal.getName());
    }
}
