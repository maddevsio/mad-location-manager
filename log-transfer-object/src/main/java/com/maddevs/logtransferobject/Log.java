package com.maddevs.logtransferobject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.maddevs.logtransferobject.types.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GpsData.class),
        @JsonSubTypes.Type(value = KalmanPredict.class),
        @JsonSubTypes.Type(value = LocationLog.class),
        @JsonSubTypes.Type(value = KalmanUpdatePredict.class),
        @JsonSubTypes.Type(value = ABSAcceleration.class)
})
@Data
public abstract class Log {
    private LogMessageType logMessageType;
    private Long timestamp;
    private String tripUuid;

    protected Log(LogMessageType logMessageType){
        this.logMessageType = logMessageType;
    }

    public abstract String toRawString();
}
