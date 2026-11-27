package com.golfclub.api.service;

import com.golfclub.api.model.Member;
import com.golfclub.api.model.Tournament;

import com.golfclub.api.repository.MemberRepository;
import com.golfclub.api.repository.TournamentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TournamentService tournamentService;

    @Test
    void shouldRejectInvalidTournamentDates() {

        Tournament tournament = new Tournament();

        tournament.setStartDate(
                LocalDate.of(2026, 12, 20)
        );

        tournament.setEndDate(
                LocalDate.of(2026, 12, 10)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> tournamentService.createTournament(tournament)
        );

        verify(tournamentRepository, never()).save(any());
    }

    @Test
    void shouldRegisterMemberToTournament() {

        Tournament tournament = new Tournament();
        tournament.setId(1L);

        Member member = new Member();
        member.setId(4L);

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        when(memberRepository.findById(4L))
                .thenReturn(Optional.of(member));

        when(tournamentRepository.save(tournament))
                .thenReturn(tournament);

        Tournament result =
                tournamentService.registerMemberToTournament(1L, 4L);

        assertTrue(
                result.getParticipatingMembers().contains(member)
        );

        verify(tournamentRepository).save(tournament);
    }

    @Test
    void shouldRejectDuplicateRegistration() {

        Tournament tournament = new Tournament();
        tournament.setId(1L);

        Member member = new Member();
        member.setId(4L);

        tournament.getParticipatingMembers().add(member);

        when(tournamentRepository.findById(1L))
                .thenReturn(Optional.of(tournament));

        when(memberRepository.findById(4L))
                .thenReturn(Optional.of(member));

        assertThrows(
                IllegalStateException.class,
                () -> tournamentService
                        .registerMemberToTournament(1L, 4L)
        );

        verify(tournamentRepository, never()).save(any());
    }
}