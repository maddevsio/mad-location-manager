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
@JsonTypeName("gyr")
@Data
public class GyrData extends Log {
    // LogMessageType.GYR_DATA
    private final BigDecimal x;
    private final BigDecimal y;
    private final BigDecimal z;
//        int tt = sscanf(str, "%" PRIu64 " GYR : x=%lf y=%lf z=%lf",
//                &sd->timestamp,
//                  &gyr->x,
//                  &gyr->y,
//                  &gyr->z);
    @Override
    public String toRawString() {
        return String.format("%s GYR : x=%s y=%s z=%s",
                getTimestamp(), x, y, z);
    }
}
