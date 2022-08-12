package com.maddevs.logtransferobject;

import com.maddevs.logtransferobject.Log;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@SuperBuilder
@Data
public class Logs {
    private List<Log> logs;
 }
