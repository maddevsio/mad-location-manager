package com.maddevs.logtransferobject;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.maddevs.logtransferobject.LogMessageType;
import com.maddevs.logtransferobject.sdf.Circle;
import com.maddevs.logtransferobject.sdf.Rectangle;
import com.maddevs.logtransferobject.types.GpsData;
import com.maddevs.logtransferobject.types.KalmanPredict;
import com.maddevs.logtransferobject.types.LocationLog;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;


@SuperBuilder
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GpsData.class),
        @JsonSubTypes.Type(value = KalmanPredict.class),
        @JsonSubTypes.Type(value = LocationLog.class)
})
@EqualsAndHashCode
public abstract class Log {
    private LogMessageType logMessageType;

    protected Log(LogMessageType logMessageType){
        this.logMessageType = logMessageType;
    }
}
