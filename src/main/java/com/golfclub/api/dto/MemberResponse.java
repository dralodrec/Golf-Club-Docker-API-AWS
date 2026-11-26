package com.golfclub.api.dto;

import com.golfclub.api.model.Member;
import com.golfclub.api.model.MembershipType;

import java.time.LocalDate;

public record MemberResponse(

        Long id,
        String memberName,
        String memberAddress,
        String memberEmailAddress,
        String memberPhoneNumber,
        LocalDate membershipStartDate,
        MembershipType membershipType

) {

    public static MemberResponse from(Member member) {

        return new MemberResponse(
                member.getId(),
                member.getMemberName(),
                member.getMemberAddress(),
                member.getMemberEmailAddress(),
                member.getMemberPhoneNumber(),
                member.getMembershipStartDate(),
                member.getMembershipType()
        );
    }
}