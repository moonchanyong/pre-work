package com.kakaopay.backend.spreadapi.domain;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResolvedHistory {
    // 사용자 아이디
    private final String userId;
    // 받은금액
    private final BigDecimal amount;
}
