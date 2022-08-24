package com.maddevs.logtransferobject.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.LogMessageType;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("abs_acceleration")
@Data
public class ABSAcceleration extends Log {
    //LogMessageType.ABS_ACC_DATA
    private BigDecimal eastAcceleration;
    private BigDecimal northAcceleration;
    private BigDecimal upAcceleration;


    @Override
    public String toRawString() {
        return String.format("%d ACC : x=%s y=%s z=%s",
                getTimestamp(),
                eastAcceleration,
                northAcceleration,
                upAcceleration
        );
    }
}
