package com.kakaopay.backend.spreadapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.Builder;
import lombok.Data;

import com.kakaopay.backend.spreadapi.domain.ResolvedHistory;

@Builder
@Data
public class SpreadingStatusResponse {
    // 뿌린 시각
    private Instant createdDate;
    // 뿌린 금액
    private BigDecimal totalAmount;
    // 받기 완료된 금액
    private BigDecimal resolvedAmount;
    // 받기 완료된 정보
    private List<ResolvedHistory> resolvedHistories;
}

