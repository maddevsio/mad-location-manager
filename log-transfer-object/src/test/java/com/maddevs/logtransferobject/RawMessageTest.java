package com.maddevs.logtransferobject;

import com.maddevs.logtransferobject.types.ABSAcceleration;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

class RawMessageTest {
    @org.junit.jupiter.api.Test
    void ABSAcceleration() {
        ABSAcceleration absAcceleration = ABSAcceleration.builder()
                .timestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                .eastAcceleration(new BigDecimal(0.154521))
                .upAcceleration(new BigDecimal(456.01524))
                .northAcceleration(new BigDecimal(-6.015753))
                .build();

        final String rawString = absAcceleration.toRawString();
        System.out.println(rawString);
    }
}
