package com.kakaopay.backend.spreadapi.controller;

import java.math.BigDecimal;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.kakaopay.backend.spreadapi.domain.ResolvedHistory;
import com.kakaopay.backend.spreadapi.domain.SpreadRoot;
import com.kakaopay.backend.spreadapi.dto.request.SpreadEventRegisterRequest;
import com.kakaopay.backend.spreadapi.dto.response.GeneratedToken;
import com.kakaopay.backend.spreadapi.dto.response.SpreadingStatusResponse;
import com.kakaopay.backend.spreadapi.service.SpreadService;
import reactor.core.publisher.Mono;

@RestController("/spread-event")
@RequiredArgsConstructor
public class SpreadController {
    private final SpreadService spreadService;
    private final ReactiveMongoTemplate mongoTemplate;

    /**
     * 받기 API
     * @param token
     * @param userId
     * @param chatRoomId
     * @return
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/receiving")
    Mono<BigDecimal> receiveAmount(
        @RequestHeader("X-SPREAD-TOKEN") String token,
        @RequestHeader("X-USER-ID") String userId,
        @RequestHeader("X-ROOM-ID") String chatRoomId
    ) {
        return spreadService.receiveAmount(userId, chatRoomId, token)
            .map(spreadRoot -> spreadRoot.findHistory(userId))
            .map(Optional::get)
            .map(ResolvedHistory::getAmount);
    }

    /**
     * 뿌리기 API
     * @param userId
     * @param chatRoomId
     * @param requestMono
     * @return
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(
        path = "/registration",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    Mono<GeneratedToken> registerSpreadEvent(
        @RequestHeader("X-USER-ID") String userId,
        @RequestHeader("X-ROOM-ID") String chatRoomId,
        @Valid @RequestBody Mono<SpreadEventRegisterRequest> requestMono
    ) {
            return spreadService.createSpreadRoot(userId, chatRoomId, requestMono)
                .flatMap(mongoTemplate::save)
                .map(SpreadRoot::convertToGeneratedToken);
    }

    /**
     * 조회 API
     * @param token 뿌리기 식별자
     * @param chatRoomId 대화방 식별자
     * @return SpreadingStatusResponse
     */
    @GetMapping
    Mono<SpreadingStatusResponse> getStatus(
        @RequestHeader("X-USER-ID") String userId,
        @RequestHeader("X-SPREAD-TOKEN") String token,
        @RequestHeader("X-ROOM-ID") String chatRoomId
    ) {
        return spreadService.findSpreadRoot(userId, chatRoomId, token)
            .map(SpreadRoot::convertToSpreadingStatusResponse);
    }
}
