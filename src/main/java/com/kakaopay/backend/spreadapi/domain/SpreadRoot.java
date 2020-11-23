package com.kakaopay.backend.spreadapi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Builder;
import lombok.Data;

import com.kakaopay.backend.spreadapi.dto.response.GeneratedToken;
import com.kakaopay.backend.spreadapi.dto.response.SpreadingStatusResponse;
import com.kakaopay.backend.spreadapi.exception.SpreadExceptionMessages;
import com.kakaopay.backend.spreadapi.util.Util;
import com.kakaopay.backend.spreadapi.util.validation.SelfTesting;
import com.kakaopay.backend.spreadapi.util.validation.Testable;

@Data
@Builder
@SelfTesting
@Document
@CompoundIndex(
    name = "valid_spread_idx",
    def = "{'token': 1, 'chatRoomId' : 1, 'createdDate': 1}")
public class SpreadRoot implements Testable {
    @Id
    private String id;
    @Version
    Long version;

    @Indexed
    private Instant createdDate;
    private final List<ResolvedHistory> resolvedHistories;
    private final List<String> receivedUser;
    private final @NotNull String token;
    private final String chatRoomId;

    private final String registerId;
    private final @NotNull BigDecimal totalAmount;
    private final @NotNull @Size(min = 1) List<BigDecimal> unResolvedAmountList;

    private BigDecimal getTotalUnresolvedAmount() {
        return unResolvedAmountList.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getTotalResolvedAmount() {
        return this.totalAmount.subtract(this.getTotalUnresolvedAmount());
    }

    public void resolveAmount(String userId, String chatRoomId) {
        //  같은 대화방만 가능.
        Util.isTrue(
            this.getChatRoomId().equals(chatRoomId),
            SpreadExceptionMessages.NOT_PARTICIPATED_CHAT_ROOM::newError);
        // 분배 할 금액이 없는 경우
        Util.isFalse(
            this.getUnResolvedAmountList().isEmpty(),
            SpreadExceptionMessages.COMPLETED_SPREAD_EVENT::newError);
        // 등록자는 받기불가
        Util.isFalse(
            this.getRegisterId().equals(userId),
            SpreadExceptionMessages.HOST_NOT_ALLOWED::newError);
        // 중복받기 확인
        Util.isFalse(
            this.hasHistory(userId),
            SpreadExceptionMessages.NESTED_USER::newError);

        final BigDecimal amount = this.getUnResolvedAmountList().remove(0);
        this.getResolvedHistories().add(new ResolvedHistory(userId, amount));
    }

    public Optional<ResolvedHistory> findHistory(String userId) {
        return this.getResolvedHistories().stream()
            .filter(history -> history.getUserId().equals(userId))
            .findFirst();
    }

    public boolean hasHistory(String userId) {
        return this.getResolvedHistories().stream()
            .anyMatch(history -> history.getUserId().equals(userId));
    }

    /**
     * Mapper
     * @return {@link GeneratedToken} Dto
     */
    public GeneratedToken convertToGeneratedToken() {
        return new GeneratedToken(this.getToken(), this.getCreatedDate());
    }

    /**
     * Mapper
     * @return {@link SpreadingStatusResponse} dto
     */
    public SpreadingStatusResponse convertToSpreadingStatusResponse() {
        return SpreadingStatusResponse.builder()
            .createdDate(this.getCreatedDate())
            .totalAmount(this.getTotalAmount())
            .resolvedAmount(this.getTotalResolvedAmount())
            .resolvedHistories(this.getResolvedHistories())
            .build();
    }

    @Override
    public void isValid() {
        final BigDecimal sumOfUnresolvedAmountList = this.getTotalUnresolvedAmount();
        final BigDecimal sumOfResolvedAmountList = this.getResolvedHistories()
            .stream()
            .map(ResolvedHistory::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        final boolean isValidConsistency = sumOfUnresolvedAmountList
            .add(sumOfResolvedAmountList)
            .compareTo(totalAmount) == 0;
        Util.isTrue(
            isValidConsistency,
            SpreadExceptionMessages.INVALID_CONSISTENCY::newError);
    }
}
