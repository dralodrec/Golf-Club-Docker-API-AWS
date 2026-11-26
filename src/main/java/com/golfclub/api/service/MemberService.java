package com.golfclub.api.service;

import com.golfclub.api.model.Member;
import com.golfclub.api.model.MembershipType;
import com.golfclub.api.repository.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // Register a new member
    @Transactional
    public Member registerMember(Member member) {
        return memberRepository.save(member);
    }

    // Retrieve all members
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    // Retrieve a member by ID
    public Member getMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Member not found with ID: " + id
                        )
                );
    }

    // Search members by name
    public List<Member> searchByName(String name) {
        return memberRepository
                .findByMemberNameContainingIgnoreCase(name);
    }

    // Search members by membership type
    public List<Member> searchByMembershipType(
            MembershipType membershipType) {

        return memberRepository
                .findByMembershipType(membershipType);
    }

    // Search members by phone number
    public List<Member> searchByPhoneNumber(String phoneNumber) {
        return memberRepository
                .findByMemberPhoneNumberContaining(phoneNumber);
    }

    // Search members by tournament start date
    public List<Member> searchByTournamentStartDate(
            LocalDate startDate) {

        return memberRepository
                .findMembersByTournamentStartDate(startDate);
    }
}