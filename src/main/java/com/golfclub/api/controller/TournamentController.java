package com.golfclub.api.controller;

import com.golfclub.api.dto.MemberResponse;
import com.golfclub.api.dto.TournamentRequest;
import com.golfclub.api.dto.TournamentResponse;

import com.golfclub.api.model.Tournament;
import com.golfclub.api.service.TournamentService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    // CREATE TOURNAMENT
    @PostMapping
    public ResponseEntity<TournamentResponse> createTournament(
            @Valid @RequestBody TournamentRequest request) {

        Tournament tournament = new Tournament();

        tournament.setStartDate(request.startDate());
        tournament.setEndDate(request.endDate());
        tournament.setLocation(request.location());
        tournament.setEntryFee(request.entryFee());
        tournament.setCashPrizeAmount(request.cashPrizeAmount());

        Tournament saved = tournamentService.createTournament(tournament);

        return ResponseEntity
                .created(URI.create("/api/tournaments/" + saved.getId()))
                .body(TournamentResponse.from(saved));
    }

    // GET ALL TOURNAMENTS
    @GetMapping
    public List<TournamentResponse> getAllTournaments() {

        return tournamentService.getAllTournaments()
                .stream()
                .map(TournamentResponse::from)
                .toList();
    }

    // GET TOURNAMENT BY ID
    @GetMapping("/{id}")
    public TournamentResponse getTournamentById(
            @PathVariable Long id) {

        return TournamentResponse.from(
                tournamentService.getTournamentById(id)
        );
    }

    // SEARCH BY START DATE
    @GetMapping("/search/date")
    public List<TournamentResponse> searchByDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate) {

        return tournamentService.searchByStartDate(startDate)
                .stream()
                .map(TournamentResponse::from)
                .toList();
    }

    // SEARCH BY LOCATION
    @GetMapping("/search/location")
    public List<TournamentResponse> searchByLocation(
            @RequestParam String location) {

        return tournamentService.searchByLocation(location)
                .stream()
                .map(TournamentResponse::from)
                .toList();
    }

    // REGISTER MEMBER TO TOURNAMENT
    @PostMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> registerMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {

        tournamentService.registerMemberToTournament(id, memberId);

        return ResponseEntity.noContent().build();
    }

    // GET TOURNAMENT PARTICIPANTS
    @GetMapping("/{id}/members")
    public List<MemberResponse> getParticipatingMembers(
            @PathVariable Long id) {

        return tournamentService.getParticipatingMembers(id)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}