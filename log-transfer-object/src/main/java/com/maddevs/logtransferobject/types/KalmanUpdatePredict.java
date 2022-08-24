package com.maddevs.logtransferobject.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maddevs.logtransferobject.Log;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("kalman_update_predict")
@Data
public class KalmanUpdatePredict extends Log {
    private static final long serialVersionUID = 4L;

    //LogMessageType.KALMAN_UPDATE
    private BigDecimal gpsLon;
    private BigDecimal gpsLat;
    private BigDecimal xVel;
    private BigDecimal yVel;
    private BigDecimal posError;
    private BigDecimal velError;

    @Override
    public String toRawString() {
        return String.format("%d KALMAN_UPDATE: gps_lon=%s gps_lat=%s " +
                        "x_vel=%s y_vel=%s " +
                        "pos_error=%s vel_error=%s",
                getTimestamp(),
                gpsLon, gpsLat, xVel, yVel, posError, velError
        );
    }
}
