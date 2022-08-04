package com.maddevs.logtransferobject;

import com.maddevs.logtransferobject.types.Record;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Log implements Serializable {
    List<Record> records;
}
