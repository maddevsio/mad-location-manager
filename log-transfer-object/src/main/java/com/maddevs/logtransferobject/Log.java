package com.maddevs.logtransferobject;

import com.maddevs.logtransferobject.LogMessageType;
import lombok.*;

import java.io.Serializable;

public interface Log extends Serializable {
    LogMessageType getLogMessageType();
}
