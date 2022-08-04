package com.maddevs.server.controllers;

import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.types.Record;
import com.maddevs.server.services.LogStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collector")
public class DataCollectorController {
    private final LogStorageService logStorageService;

    @PostMapping("/{deviceId}")
    public void storeLog(@PathVariable String deviceId, @RequestBody List<Record> records) {
        logStorageService.save(deviceId, records);
    }
}