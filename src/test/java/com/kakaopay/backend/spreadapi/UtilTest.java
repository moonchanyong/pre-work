package com.kakaopay.backend.spreadapi;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.kakaopay.backend.spreadapi.util.Util;

class UtilTest {

    @DisplayName("토큰 생성 로직 테스트")
    @Test
    void testGenerateToken() {
        final String token = Util.generateToken();
        Assertions.assertEquals(3, token.length());
    }

    @DisplayName("분배로직 테스트")
    @Test
    void testSplitAmount() {
        final List<BigDecimal> result = Util.splitAmount(new BigDecimal(100), 2);
        Assertions.assertEquals(List.of(BigDecimal.valueOf(50), BigDecimal.valueOf(50)), result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme