package com.golfclub.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TournamentRequest(

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotBlank
        String location,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal entryFee,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal cashPrizeAmount

) {}