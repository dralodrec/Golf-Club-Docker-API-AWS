package com.golfclub.api.dto;

import com.golfclub.api.model.MembershipType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record MemberRequest(

        @NotBlank
        String memberName,

        @NotBlank
        String memberAddress,

        @NotBlank
        @Email
        String memberEmailAddress,

        @NotBlank
        String memberPhoneNumber,

        @NotNull
        LocalDate membershipStartDate,

        @NotNull
        MembershipType membershipType

) {}