package com.maddevs.server.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document("log_items")
public class LogRecordEntity {

    @Id
    private String id;

    private String deviceId;

    private Long timestamp;

    private String tripUuid;

    private String payload;
}
