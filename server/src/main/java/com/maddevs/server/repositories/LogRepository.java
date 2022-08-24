package com.maddevs.server.repositories;

import com.maddevs.logtransferobject.LogMessageType;
import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.entities.SumPrice;
import com.maddevs.server.entities.Trip;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface LogRepository extends MongoRepository<LogRecordEntity, String> {

    @Query("{device:'?0'}")
    LogRecordEntity findItemByDevice(String device);

    List<LogRecordEntity> getAllByDeviceIdAndTripUuid(String device, String tripUuid);

    @Query(value="{type:'?0'}", fields="{'datetime' : 1}")
    List<LogRecordEntity> findAll(LogMessageType logType);

    @Aggregation(pipeline = {
            "{$group: {\n" +
                    "            _id: \"$tripUuid\",\n" +
                    "            device: { $min: \"$deviceId\" },\n" +
                    "            begin: { $min: \"$timestamp\" },\n" +
                    "            end: { $max: \"$timestamp\" },\n" +
                    "            size: { $count: { } }\n" +
                    "        }}"
    })
    AggregationResults<Trip> findAllTrip();
}
