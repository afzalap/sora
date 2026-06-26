package com.sora.backend.dto;

import com.sora.backend.flight.FlightOption;

import java.util.List;

public record ChatResponse(String reply, List<FlightOption> flights) {}
