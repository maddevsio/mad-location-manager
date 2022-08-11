package com.maddevs.logtransferobject.types;

import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.LogMessageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class LocationLog implements Log {
    private static final long serialVersionUID = 3L;


    @Override
    public LogMessageType getLogMessageType() {
        return LogMessageType.ABS_ACC_DATA;
    }
}
