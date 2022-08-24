package com.maddevs.server.dto;

import lombok.Data;

@Data
public class TripDto {
    private String deviceId;
    private String tripStart;
    private String tripEnd;
    private Long records;
}

