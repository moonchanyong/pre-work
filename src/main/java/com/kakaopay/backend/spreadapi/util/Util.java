package com.kakaopay.backend.spreadapi.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.Assert;

public final class Util {
    private Util() {}
    static final int leftLimit = 33; // letter 'A'
    static final int rightLimit = 126; // letter 'z'
    static final int TOKEN_LENGTH = 3;

    public static void isTrue(
        boolean expression,
        Supplier<RuntimeException> exceptionSupplier
    ) {
        if (!expression) {
            throw exceptionSupplier.get();
        }
    }

    public static void isFalse(
        boolean expression,
        Supplier<RuntimeException> exceptionSupplier
    ) {
        if (expression) {
            throw exceptionSupplier.get();
        }
    }
    public static String generateToken() {
        final Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
            .limit(TOKEN_LENGTH)
            .collect(
                StringBuilder::new,
                StringBuilder::appendCodePoint,
                StringBuilder::append)
            .toString();
    }

    /**
     *
     * @param totalAmount 뿌릴 금액
     * @param numberOfSplits 뿌릴 인원
     * @return 랜덤 정렬된 금액 리스트
     */
    public static List<BigDecimal> splitAmount(BigDecimal totalAmount, int numberOfSplits) {
        Assert.isTrue(numberOfSplits != 0);
        Assert.isTrue(totalAmount.compareTo(BigDecimal.valueOf(numberOfSplits)) >= 0);

        final BigDecimal amountPerUser = totalAmount.divide(
                BigDecimal.valueOf(numberOfSplits),
                0,
                RoundingMode.FLOOR);

        int restAmount = totalAmount
            .remainder(BigDecimal.valueOf(numberOfSplits))
            .intValue();

        final List<BigDecimal> amounts =
            Stream.generate(() -> amountPerUser.add(BigDecimal.ZERO))
                .limit(numberOfSplits)
                .collect(Collectors.toList());

        // assert restAmount is less than the numberOfSplits.
        // assert size of amounts is equal to the numberOfSplits.
        while (restAmount > 0) {
            final int idx = restAmount--;
            amounts.set(idx, amounts.get(idx).add(BigDecimal.ONE));
        }

        // assert the intStream must have one integer number at least.
        return amounts.stream()
            .sorted((a, b) -> new Random().ints(-1, 1).findFirst().getAsInt())
            .collect(Collectors.toList());
    }
}
