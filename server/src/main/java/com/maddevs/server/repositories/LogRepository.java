package com.maddevs.server.repositories;

import com.maddevs.logtransferobject.LogMessageType;
import com.maddevs.server.entities.LogRecordEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface LogRepository extends MongoRepository<LogRecordEntity, String> {

    @Query("{device:'?0'}")
    LogRecordEntity findItemByDevice(String device);

    @Query(value="{type:'?0'}", fields="{'datetime' : 1}")
    List<LogRecordEntity> findAll(LogMessageType logType);

}
