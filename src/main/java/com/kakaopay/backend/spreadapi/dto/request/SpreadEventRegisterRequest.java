package com.kakaopay.backend.spreadapi.dto.request;

import static com.kakaopay.backend.spreadapi.exception.SpreadExceptionMessages.*;

import java.math.BigDecimal;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.kakaopay.backend.spreadapi.util.Util;
import com.kakaopay.backend.spreadapi.util.validation.SelfTesting;
import com.kakaopay.backend.spreadapi.util.validation.Testable;


@SelfTesting
@Getter
@AllArgsConstructor
public class SpreadEventRegisterRequest implements Testable {
    private final @DecimalMin("1") @NotNull BigDecimal totalAmount;
    private final @Min(1) int numberOfSplit;

    @Override
    public void isValid() {
        // 뿌릴 금액은 받을사람보다 더 커야한다.
        Util.isTrue(
            totalAmount.compareTo(BigDecimal.valueOf(numberOfSplit)) >= 0,
            INVALID_REGISTRATION_REQUEST::newError);
    }
}
