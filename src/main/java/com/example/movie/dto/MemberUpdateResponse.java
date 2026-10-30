package com.example.movie.dto;

import com.example.movie.model.Member;

public record MemberUpdateResponse(
        Integer memberId,
        String username,
        String name,
        String phone,
        String email,
        boolean passwordChanged)
{
    public static MemberUpdateResponse from(Member m, boolean passwordChanged) {
        return new MemberUpdateResponse(
                m.getMemberId(),
                m.getUsername(),
                m.getName(),
                m.getPhone(),
                m.getEmail(),
                passwordChanged);
    }
}
