package com.maddevs.server.workers;

import com.maddevs.server.entities.LogRecordEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;


public interface ExportWorker {
    InputStream toInputStream(List<LogRecordEntity> logs);
}
