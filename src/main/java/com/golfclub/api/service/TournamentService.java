package com.golfclub.api.service;

import com.golfclub.api.model.Member;
import com.golfclub.api.model.Tournament;

import com.golfclub.api.repository.MemberRepository;
import com.golfclub.api.repository.TournamentRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final MemberRepository memberRepository;

    public TournamentService(
            TournamentRepository tournamentRepository,
            MemberRepository memberRepository) {

        this.tournamentRepository = tournamentRepository;
        this.memberRepository = memberRepository;
    }

    // Create a tournament
    @Transactional
    public Tournament createTournament(Tournament tournament) {

        if (tournament.getStartDate() != null
                && tournament.getEndDate() != null
                && tournament.getEndDate()
                .isBefore(tournament.getStartDate())) {

            throw new IllegalArgumentException(
                    "Tournament end date cannot be before start date"
            );
        }

        return tournamentRepository.save(tournament);
    }

    // Retrieve all members registered for a tournament
    public List<Member> getParticipatingMembers(Long tournamentId) {

        Tournament tournament = getTournamentById(tournamentId);

        return List.copyOf(tournament.getParticipatingMembers());
    }

    // Retrieve all tournaments
    public List<Tournament> getAllTournaments() {
        return tournamentRepository.findAll();
    }

    // Retrieve tournament by ID
    public Tournament getTournamentById(Long id) {

        return tournamentRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Tournament not found with ID: " + id
                        )
                );
    }

    // Search tournaments by start date
    public List<Tournament> searchByStartDate(LocalDate startDate) {

        return tournamentRepository.findByStartDate(startDate);
    }

    // Search tournaments by location
    public List<Tournament> searchByLocation(String location) {

        return tournamentRepository
                .findByLocationContainingIgnoreCase(location);
    }

    // Register an existing member to a tournament
    @Transactional
    public Tournament registerMemberToTournament(
            Long tournamentId,
            Long memberId) {

        Tournament tournament = getTournamentById(tournamentId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Member not found with ID: " + memberId
                        )
                );

        boolean alreadyRegistered =
                tournament.getParticipatingMembers()
                        .stream()
                        .anyMatch(existing ->
                                memberId.equals(existing.getId()));

        if (alreadyRegistered) {
            throw new IllegalStateException(
                    "Member is already registered for this tournament"
            );
        }

        tournament.getParticipatingMembers().add(member);

        return tournamentRepository.save(tournament);
    }
}