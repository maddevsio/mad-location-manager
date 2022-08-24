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
@JsonTypeName("mag")
@Data
public class MagData extends Log {
    private static final long serialVersionUID = 1L;
    // LogMessageType.MAGNITUDE_DATA
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal z;


    @Override
    public String toRawString() {
        return String.format("% MAG : x=%s y=%s z=%s",
                getTimestamp(),
                x, y, z);
    }
}
