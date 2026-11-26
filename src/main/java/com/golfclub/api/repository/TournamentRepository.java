package com.golfclub.api.repository;

import com.golfclub.api.model.Tournament;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TournamentRepository
        extends JpaRepository<Tournament, Long> {

    // Search tournaments by start date
    List<Tournament> findByStartDate(
            LocalDate startDate
    );

    // Search tournaments by location
    List<Tournament> findByLocationContainingIgnoreCase(
            String location
    );
}