package com.kakaopay.backend.spreadapi.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GeneratedToken {
    private String token;
    private Instant createdDate;
}
