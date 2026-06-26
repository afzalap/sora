package com.sora.backend.dto;

import java.util.List;

public record ChatRequest(String message, List<HistoryMessage> history) {
    public record HistoryMessage(String role, String content) {}
}
