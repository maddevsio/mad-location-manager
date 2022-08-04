package com.maddevs.server.mappers;

import com.maddevs.logtransferobject.types.Record;
import com.maddevs.server.entities.LogRecordEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LogRecordMapper {
    LogRecordEntity sourceToDestination(Record source);
}
