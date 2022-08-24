package com.maddevs.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maddevs.logtransferobject.Log;
import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.entities.Trip;
import com.maddevs.server.mappers.LogRecordMapper;
import com.maddevs.server.repositories.LogRepository;
import com.maddevs.server.repositories.SalesPoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LogStorageService {
    private final LogRepository logRepository;
    private final SalesPoRepository salesPoRepository;
    private final LogRecordMapper logRecordMapper;
    private final ObjectMapper objectMapper;

    public List<LogRecordEntity> save(String deviceId, List<Log> logs) throws Exception {
        final Collection<LogRecordEntity> logRecordEntities = logs.stream()
                .map(r->{
                    final LogRecordEntity logRecordEntity = logRecordMapper.sourceToDestination(r);
                    logRecordEntity.setDeviceId(deviceId);
                    try {
                        logRecordEntity.setPayload(objectMapper.writeValueAsString(r));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return logRecordEntity;
                })
                .collect(Collectors.toList());

        return this.logRepository.saveAll(logRecordEntities);
    }

    public LogRecordEntity getListLogsByDeviceId(String deviceId) {
        return this.logRepository.findItemByDevice(deviceId);
    }

    public List<Trip> getTripsInfo() {
        final List<Trip> allTrip = this.logRepository.findAllTrip().getMappedResults();
        System.out.println(allTrip);
        return Collections.EMPTY_LIST;

    }

    public List<LogRecordEntity> getLogByTrip(String deviceId, String tripUuid) {
        //Long milliseconds = timeStart.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
        return this.logRepository.getAllByDeviceIdAndTripUuid(deviceId, tripUuid);
    }
}
