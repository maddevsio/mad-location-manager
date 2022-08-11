package com.maddevs.logtransferobject.types;

import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.LogMessageType;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class GpsData implements Log {
    private static final long serialVersionUID = 1L;


    @Override
    public LogMessageType getLogMessageType() {
        return LogMessageType.GPS_DATA;
    }
}
