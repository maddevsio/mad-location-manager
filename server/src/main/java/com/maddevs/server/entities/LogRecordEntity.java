package com.maddevs.server.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("log_items")
public class LogRecordEntity {

    @Id
    private String id;

    private String deviceId;
}
