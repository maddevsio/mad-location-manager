package com.maddevs.server.mappers;

import com.maddevs.logtransferobject.Log;
import com.maddevs.server.dto.TripDto;
import com.maddevs.server.entities.LogRecordEntity;
import com.maddevs.server.entities.Trip;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TripMapper {
    TripDto sourceToDestination(Trip source);
}
