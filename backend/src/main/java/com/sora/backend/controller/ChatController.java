package com.sora.backend.controller;

import com.sora.backend.dto.ChatRequest;
import com.sora.backend.dto.ChatResponse;
import com.sora.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request, Principal principal) {
        return chatService.chat(request.message(), principal.getName());
    }
}
