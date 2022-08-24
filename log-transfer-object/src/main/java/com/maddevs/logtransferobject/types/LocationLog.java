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
@JsonTypeName("location")
@Data
public class LocationLog extends Log {
    private static final long serialVersionUID = 3L;

    private BigDecimal bearing;
    private BigDecimal speed;
    private BigDecimal accuracy;
    private BigDecimal elapsedRealtimeNanos;
    private BigDecimal lat;
    private BigDecimal lon;
    private BigDecimal alt;

    @Override
    public String toRawString() {
            return String.format("%s Location : pos lat=%s lon=%s alt=%s " +
                            "hdop=%s speed=%s bearing=%s",
            getTimestamp(), lat, lon, alt, accuracy, speed, bearing);
    }
}
