package com.golfclub.api.dto;

import com.golfclub.api.model.Tournament;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TournamentResponse(

        Long id,
        LocalDate startDate,
        LocalDate endDate,
        String location,
        BigDecimal entryFee,
        BigDecimal cashPrizeAmount

) {

    public static TournamentResponse from(
            Tournament tournament) {

        return new TournamentResponse(
                tournament.getId(),
                tournament.getStartDate(),
                tournament.getEndDate(),
                tournament.getLocation(),
                tournament.getEntryFee(),
                tournament.getCashPrizeAmount()
        );
    }
}