package com.kakaopay.backend.spreadapi.service;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.kakaopay.backend.spreadapi.config.DataInitialize;
import com.kakaopay.backend.spreadapi.domain.SpreadRoot;
import com.kakaopay.backend.spreadapi.dto.request.SpreadEventRegisterRequest;
import reactor.core.publisher.Mono;

@Import(DataInitialize.class)
@DataMongoTest(includeFilters = @ComponentScan.Filter(Service.class))
class SpreadServiceTest {
    @Autowired SpreadService spreadService;
    @Autowired ReactiveMongoTemplate mongoTemplate;
    @Autowired DataInitialize dataInitialize;

    @DisplayName("7일이내 뿌리기이벤트 조회 테스트")
    @Test
    void testFindSpreadRoot() {
        final SpreadRoot result = spreadService
            .findSpreadRoot(
                dataInitialize.getUserId(),
                dataInitialize.getChatRoomId(),
                dataInitialize.getToken())
            .block();

        Assertions.assertNotNull(result);
    }

    @DisplayName("7일이 지난 뿌리기이벤트 조회 테스트")
    @Test
    void testFindInvalidSpreadRoot() {
        Assertions.assertThrows(ResponseStatusException.class, () -> {
            final SpreadRoot result = spreadService
                .findSpreadRoot(
                    dataInitialize.getUserId(),
                    dataInitialize.getChatRoomId(),
                    dataInitialize.getExpiredToken())
                .block();
        });
    }

    @DisplayName("`SpreadRoot` 생성 테스트")
    @Test
    void testCreateSpreadRoot() {
        final BigDecimal totalAmount = BigDecimal.valueOf(300000);
        final int numberOfSplit = 17;
        final var reqMono = Mono.just(
            new SpreadEventRegisterRequest(totalAmount, numberOfSplit));

        final SpreadRoot createdSpreadRoot = spreadService
            .createSpreadRoot("userId", "chatRoomId", reqMono)
            .flatMap(mongoTemplate::save)
            .block();

        Assertions.assertNotNull(createdSpreadRoot);
        createdSpreadRoot.isValid();
    }

    @DisplayName("받기 테스트")
    @Test
    void receiveAmount() {
        final SpreadRoot spreadRoot = spreadService.receiveAmount(
            "ryan",
            dataInitialize.getChatRoomId(),
            dataInitialize.getToken())
            .block();

        Assertions.assertNotNull(spreadRoot);
        Assertions.assertFalse(spreadRoot.getResolvedHistories().isEmpty());
        spreadRoot.isValid();
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme