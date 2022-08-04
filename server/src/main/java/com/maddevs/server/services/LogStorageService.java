package com.maddevs.server.services;

import com.maddevs.logtransferobject.types.Record;
import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.mappers.LogRecordMapper;
import com.maddevs.server.repositories.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class LogStorageService {
    private final LogRepository logRepository;
    private final LogRecordMapper logRecordMapper;

    public void save(String deviceId, List<Record> records) {
        final Collection<LogRecordEntity> logRecordEntities = records.stream()
                .map(r->{
                    final LogRecordEntity logRecordEntity = logRecordMapper.sourceToDestination(r);
                    logRecordEntity.setDeviceId(deviceId);
                    return logRecordEntity;
                })
                .collect(Collectors.toList());

        this.logRepository.saveAll(logRecordEntities);
    }

    public LogRecordEntity getListLogsByDeviceId(String deviceId) {
        return this.logRepository.findItemByDevice(deviceId);
    }
}
