package com.maddevs.logtransferobject.types;

import com.maddevs.logtransferobject.LogMessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LocationRecord extends Record {

    public LocationRecord(String payload) {
        super(LogMessageType.ABS_ACC_DATA, payload);
    }
}
