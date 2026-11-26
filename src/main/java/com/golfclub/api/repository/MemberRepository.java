package com.golfclub.api.repository;

import com.golfclub.api.model.Member;
import com.golfclub.api.model.MembershipType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MemberRepository
        extends JpaRepository<Member, Long> {

    // Search members by name
    List<Member> findByMemberNameContainingIgnoreCase(
            String name
    );

    // Search members by membership type
    List<Member> findByMembershipType(
            MembershipType membershipType
    );

    // Search members by phone number
    List<Member> findByMemberPhoneNumberContaining(
            String phoneNumber
    );

    // Search members by tournament start date
    @Query("""
        SELECT DISTINCT m
        FROM Tournament t
        JOIN t.participatingMembers m
        WHERE t.startDate = :startDate
        """)
    List<Member> findMembersByTournamentStartDate(
            @Param("startDate") LocalDate startDate
    );
}