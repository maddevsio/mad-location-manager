package com.maddevs.logtransferobject.types;

import com.maddevs.logtransferobject.LogMessageType;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Record implements Serializable {

    private LogMessageType logMessageType;

    private String payload;

    public Record(LogMessageType logMessageType, String payload) {
        this.logMessageType = logMessageType;
        this.payload = payload;
    }
}
