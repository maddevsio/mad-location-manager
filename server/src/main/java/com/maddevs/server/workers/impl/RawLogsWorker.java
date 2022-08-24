package com.maddevs.server.workers.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maddevs.logtransferobject.Log;
import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.workers.ExportWorker;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class RawLogsWorker implements ExportWorker {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public InputStream toInputStream(List<LogRecordEntity> logs) {
        StringBuilder sb = new StringBuilder();
        logs.forEach(l->sb.append(getRawPayload(l)+System.lineSeparator()));

        return new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private String getRawPayload(LogRecordEntity record) {
        final Log log = objectMapper.readValue(record.getPayload(), Log.class);
        return log.toRawString();
    }
}
