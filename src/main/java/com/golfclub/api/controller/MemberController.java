package com.golfclub.api.controller;

import com.golfclub.api.dto.MemberRequest;
import com.golfclub.api.dto.MemberResponse;

import com.golfclub.api.model.Member;
import com.golfclub.api.model.MembershipType;

import com.golfclub.api.service.MemberService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // POST - Register a new member
    @PostMapping
    public ResponseEntity<MemberResponse> registerMember(
            @Valid @RequestBody MemberRequest request) {

        Member member = new Member();

        member.setMemberName(request.memberName());
        member.setMemberAddress(request.memberAddress());
        member.setMemberEmailAddress(request.memberEmailAddress());
        member.setMemberPhoneNumber(request.memberPhoneNumber());
        member.setMembershipStartDate(request.membershipStartDate());
        member.setMembershipType(request.membershipType());

        Member savedMember = memberService.registerMember(member);

        return ResponseEntity
                .created(URI.create("/api/members/" + savedMember.getId()))
                .body(MemberResponse.from(savedMember));
    }

    // GET - Retrieve all members
    @GetMapping
    public List<MemberResponse> getAllMembers() {

        return memberService.getAllMembers()
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    // GET - Retrieve member by ID
    @GetMapping("/{id}")
    public MemberResponse getMemberById(@PathVariable Long id) {

        return MemberResponse.from(
                memberService.getMemberById(id)
        );
    }

    // GET - Search by name
    @GetMapping("/search/name")
    public List<MemberResponse> searchByName(
            @RequestParam String name) {

        return memberService.searchByName(name)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    // GET - Search by membership type
    @GetMapping("/search/type")
    public List<MemberResponse> searchByType(
            @RequestParam MembershipType type) {

        return memberService.searchByMembershipType(type)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    // GET - Search by phone number
    @GetMapping("/search/phone")
    public List<MemberResponse> searchByPhone(
            @RequestParam String phone) {

        return memberService.searchByPhoneNumber(phone)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    // GET - Search by tournament start date
    @GetMapping("/search/tournament-date")
    public List<MemberResponse> searchByTournamentDate(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate) {

        return memberService
                .searchByTournamentStartDate(startDate)
                .stream()
                .map(MemberResponse::from)
                .toList();
    }
}