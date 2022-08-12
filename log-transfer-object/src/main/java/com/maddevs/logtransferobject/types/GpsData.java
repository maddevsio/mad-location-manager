package com.maddevs.logtransferobject.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.LogMessageType;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("gps")
@Data
public class GpsData extends Log {
    private static final long serialVersionUID = 1L;

//    @Builder
//    public GpsData(){
//        super(LogMessageType.GPS_DATA);
//    }
}
