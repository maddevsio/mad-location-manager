package com.maddevs.logtransferobject;

import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@SuperBuilder
@Data
public class Logs {
    private List<Log> logs;
 }
