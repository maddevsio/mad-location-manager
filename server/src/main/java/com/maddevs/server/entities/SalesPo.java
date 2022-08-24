package com.maddevs.server.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "SalesPo")
@Data
public class SalesPo {
    @Id
    private String id;
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate poDate;
    private BigDecimal amount;
}
