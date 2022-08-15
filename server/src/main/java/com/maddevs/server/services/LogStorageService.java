package com.maddevs.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maddevs.logtransferobject.Log;
import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.mappers.LogRecordMapper;
import com.maddevs.server.repositories.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LogStorageService {
    private final LogRepository logRepository;
    private final LogRecordMapper logRecordMapper;
    private final ObjectMapper objectMapper;

    public List<LogRecordEntity> save(String deviceId, List<Log> logs) throws IOException {
        final Collection<LogRecordEntity> logRecordEntities = logs.stream()
                .map(r->{
                    final LogRecordEntity logRecordEntity = logRecordMapper.sourceToDestination(r);
                    logRecordEntity.setDeviceId(deviceId);
                    return logRecordEntity;
                })
                .collect(Collectors.toList());

        return this.logRepository.saveAll(logRecordEntities);
    }

    public LogRecordEntity getListLogsByDeviceId(String deviceId) {
        return this.logRepository.findItemByDevice(deviceId);
    }
}
