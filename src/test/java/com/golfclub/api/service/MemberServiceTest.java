package com.golfclub.api.service;

import com.golfclub.api.model.Member;
import com.golfclub.api.repository.MemberRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void shouldRegisterMember() {

        Member member = new Member();
        member.setMemberName("John Smith");

        when(memberRepository.save(member))
                .thenReturn(member);

        Member result = memberService.registerMember(member);

        assertEquals("John Smith", result.getMemberName());

        verify(memberRepository).save(member);
    }

    @Test
    void shouldFindMemberById() {

        Member member = new Member();
        member.setId(1L);
        member.setMemberName("John Smith");

        when(memberRepository.findById(1L))
                .thenReturn(Optional.of(member));

        Member result = memberService.getMemberById(1L);

        assertEquals(1L, result.getId());
        assertEquals("John Smith", result.getMemberName());
    }

    @Test
    void shouldThrowExceptionWhenMemberNotFound() {

        when(memberRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> memberService.getMemberById(999L)
        );

        verify(memberRepository).findById(999L);
    }

    @Test
    void shouldSearchMembersByName() {

        Member member = new Member();
        member.setMemberName("John Smith");

        when(memberRepository
                .findByMemberNameContainingIgnoreCase("John"))
                .thenReturn(List.of(member));

        List<Member> results =
                memberService.searchByName("John");

        assertEquals(1, results.size());
        assertEquals("John Smith",
                results.get(0).getMemberName());
    }
}