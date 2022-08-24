package com.maddevs.server.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Trip {
    @Id
    private String trip;
    private String device;
    private String begin;
    private String end;
    private Integer size;
}
