package com.golfclub.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Entity
@Table(name = "members")

public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String memberName;

    @NotBlank
    private String memberAddress;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String memberEmailAddress;

    @NotBlank
    private String memberPhoneNumber;

    @NotNull
    private LocalDate membershipStartDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MembershipType membershipType;

    public Member() {
    }

    // Generate getters and setters here

    public Long getId() {
        return id;
    }

    public String getMemberAddress() {
        return memberAddress;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberEmailAddress() {
        return memberEmailAddress;
    }

    public String getMemberPhoneNumber() {
        return memberPhoneNumber;
    }

    public LocalDate getMembershipStartDate() {
        return membershipStartDate;
    }

    public MembershipType getMembershipType() {
        return membershipType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMemberAddress(String memberAddress) {
        this.memberAddress = memberAddress;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setMemberEmailAddress(String memberEmailAddress) {
        this.memberEmailAddress = memberEmailAddress;
    }

    public void setMemberPhoneNumber(String memberPhoneNumber) {
        this.memberPhoneNumber = memberPhoneNumber;
    }

    public void setMembershipStartDate(LocalDate membershipStartDate) {
        this.membershipStartDate = membershipStartDate;
    }

    public void setMembershipType(MembershipType membershipType) {
        this.membershipType = membershipType;
    }
}
