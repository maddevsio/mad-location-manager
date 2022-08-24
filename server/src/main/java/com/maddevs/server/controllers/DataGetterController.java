package com.maddevs.server.controllers;

import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.mappers.TripMapper;
import com.maddevs.server.services.LogStorageService;
import com.maddevs.server.workers.ExportWorker;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.maddevs.server.dto.*;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/getter")
public class DataGetterController {
    private final LogStorageService logStorageService;
    private final TripMapper tripMapper;
    private final ExportWorker exportWorker;


    @GetMapping("/trips")
    public List<TripDto> getListOfTrips() {
        return logStorageService.getTripsInfo().stream()
                .map(t->tripMapper.sourceToDestination(t))
                .collect(Collectors.toList());
    }

    @GetMapping(
            value = "/trips/{deviceId}/{tripUuid}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> getListOfTrip(@PathVariable String deviceId, @PathVariable String tripUuid) throws IOException {
        final List<LogRecordEntity> logByTrip = this.logStorageService.getLogByTrip(deviceId, tripUuid);
        InputStream in = this.exportWorker.toInputStream(logByTrip);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set(HttpHeaders.CONTENT_DISPOSITION,
                String.format("inline; filename=\"raw_logs_%s-%s.txt\"",
                        deviceId, tripUuid
                ));

        return new ResponseEntity<>(IOUtils.toByteArray(in), responseHeaders, HttpStatus.OK);
    }
}
