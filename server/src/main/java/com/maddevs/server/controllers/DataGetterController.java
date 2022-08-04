package com.maddevs.server.controllers;

import com.maddevs.logtransferobject.types.Record;
import com.maddevs.server.services.LogStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/getter")
public class DataGetterController {
    private final LogStorageService logStorageService;

    @GetMapping("/{deviceId}")
    public void getListOfLogs(@PathVariable String deviceId) {
        logStorageService.getListLogsByDeviceId(deviceId);
    }
}
