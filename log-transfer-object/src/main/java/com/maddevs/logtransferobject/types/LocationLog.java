package com.maddevs.logtransferobject.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.LogMessageType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeName("location")
@Data
public class LocationLog extends Log {
    private static final long serialVersionUID = 3L;
}
