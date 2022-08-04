package com.maddevs.logtransferobject.types;

import com.maddevs.logtransferobject.LogMessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KalmanPredict extends Record {

    public KalmanPredict(String payload) {
        super(LogMessageType.KALMAN_PREDICT, payload);
    }
}
