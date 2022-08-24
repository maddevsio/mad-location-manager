package com.maddevs.logtransferobject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maddevs.logtransferobject.types.GpsData;
import com.maddevs.logtransferobject.types.KalmanPredict;
import com.maddevs.logtransferobject.types.LocationLog;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class SerializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final List<Log> data = new ArrayList<>();
    static {
        data.add(KalmanPredict.builder().absEastAcceleration(BigDecimal.valueOf(15.7)).build());
        data.add(LocationLog.builder().build());
        data.add(GpsData.builder().build());
    }

    @SneakyThrows
    @org.junit.jupiter.api.Test
    void process() {
        Logs expected = Logs.builder().logs(data).build();
        final String content = objectMapper.writeValueAsString(expected);
        System.out.println(content);

        final Logs actual = objectMapper.readValue(content, Logs.class);

        Assertions.assertTrue(expected.equals(actual));
    }
}
