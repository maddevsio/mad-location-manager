package com.maddevs.logtransferobject.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.LogMessageType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("kalman_predict")
@Data
public class KalmanPredict extends Log {
    private static final long serialVersionUID = 2L;

    private BigDecimal absEastAcceleration;
    private BigDecimal absNorthAcceleration;
    private BigDecimal absUpAcceleration;


}
