package com.kakaopay.backend.spreadapi.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;

import com.kakaopay.backend.spreadapi.domain.SpreadRoot;
import com.kakaopay.backend.spreadapi.dto.request.SpreadEventRegisterRequest;
import com.kakaopay.backend.spreadapi.service.SpreadService;
import reactor.core.publisher.Mono;

@Component
public class DataInitialize {
    private final String userId = "chanyong.moon";
    private final String chatRoomId = "myRoom";
    private final String token;
    private final String expiredToken;

    @Autowired
    private DataInitialize(
        ReactiveMongoTemplate mongoTemplate,
        SpreadService spreadService
    ) {
        final BigDecimal totalAmount = BigDecimal.valueOf(300000);
        final int numberOfSplit = 17;
        final var reqMono = Mono.just(
            new SpreadEventRegisterRequest(totalAmount, numberOfSplit));

        // 방금 생성된 뿌리기이벤트
        final SpreadRoot spreadRoot =
            spreadService.createSpreadRoot(userId, chatRoomId, reqMono)
                .flatMap(mongoTemplate::save)
                .block();

        // 7일이 지난 뿌리기이벤트
        final SpreadRoot spreadRoot1 =
            spreadService.createSpreadRoot(userId, chatRoomId, reqMono)
                .doOnNext(spreadRoot2 ->
                    spreadRoot2.setCreatedDate(Instant.now().minus(8, ChronoUnit.DAYS)))
                .flatMap(mongoTemplate::save)
                .block();

        Assertions.assertNotNull(spreadRoot);
        Assertions.assertNotNull(spreadRoot1);
        token = spreadRoot.getToken();
        expiredToken = spreadRoot1.getToken();
    }

    public String getToken() {
        return token;
    }

    public String getExpiredToken() {
        return expiredToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getChatRoomId() {
        return chatRoomId;
    }
}
