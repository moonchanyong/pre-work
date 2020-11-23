package com.kakaopay.backend.spreadapi.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;

import lombok.RequiredArgsConstructor;

import com.kakaopay.backend.spreadapi.domain.SpreadRoot;
import com.kakaopay.backend.spreadapi.dto.request.SpreadEventRegisterRequest;
import com.kakaopay.backend.spreadapi.exception.SpreadExceptionMessages;
import com.kakaopay.backend.spreadapi.util.Util;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RequiredArgsConstructor
@Service
public class SpreadService {
    private final ReactiveMongoTemplate mongoTemplate;

    /**
     * 주어진 시간에 내에 생성 된 뿌리기 이벤트 조회
     * @param chatRoomId 대화방 식별자
     * @param token 뿌리기 이벤트 식별자
     * @param time {@link ChronoUnit}의 시간 양
     * @param unit 단위시간
     * @return 유효한 {@link SpreadRoot}
     */
    private static Criteria getQueryOfValidSpreadRoot(
        String chatRoomId,
        String token,
        int time,
        ChronoUnit unit
    ) {
        final Instant now = Instant.now();
        final Instant sevenDayAgo = Instant.now().minus(time, unit);
        return new Criteria("chatRoomId").is(chatRoomId)
                .and("token").is(token)
                .and("createdDate").gt(sevenDayAgo).lt(now);
    }

    public Mono<SpreadRoot> findSpreadRoot(
        String userId,
        String chatRoomId,
        String token
    ) {
        return mongoTemplate.findOne(
            new Query(
                getQueryOfValidSpreadRoot(chatRoomId, token, 7, ChronoUnit.DAYS)
                .and("registerId").is(userId)),
            SpreadRoot.class)
            .switchIfEmpty(Mono.error(SpreadExceptionMessages.NOT_EXISTS::newError));
    }

    /**
     *
     * @param userId        사용자 식별자
     * @param chatRoomId    대화방 식별자
     * @param requestMono   뿌리기 이벤트 요청정보
     * @return
     */
    public Mono<SpreadRoot> createSpreadRoot(
        String userId,
        String chatRoomId,
        Mono<SpreadEventRegisterRequest> requestMono
    ) {
        return requestMono
            .map(req -> SpreadRoot.builder()
                .createdDate(Instant.now())
                .chatRoomId(chatRoomId)
                .registerId(userId)
                .token(Util.generateToken())
                .totalAmount(req.getTotalAmount())
                .unResolvedAmountList(
                    Util.splitAmount(
                        req.getTotalAmount(),
                        req.getNumberOfSplit()))
                .resolvedHistories(List.of())
                .build());
    }

    public Mono<SpreadRoot> receiveAmount(
        String userId,
        String chatRoomId,
        String token
    ) {
        return mongoTemplate.findOne(
                new Query(
                    getQueryOfValidSpreadRoot(chatRoomId, token, 10, ChronoUnit.MINUTES)),
                SpreadRoot.class)
            .log()
            .onErrorResume(throwable -> Mono.empty())
            // 유효한 SpreadRoot가 없음면 에러
            .switchIfEmpty(Mono.error(SpreadExceptionMessages.NOT_EXISTS::newError))
            .doOnNext(spreadRoot -> spreadRoot.resolveAmount(userId, chatRoomId))
            .flatMap(mongoTemplate::save)
            .retryWhen(Retry.fixedDelay(10, Duration.ofSeconds(10))
                .filter(error -> error instanceof OptimisticLockingFailureException));
    }
}
