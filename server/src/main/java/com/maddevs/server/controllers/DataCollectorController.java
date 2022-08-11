package com.maddevs.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.maddevs.logtransferobject.Log;
import com.maddevs.logtransferobject.Zipper;
import com.maddevs.server.services.LogStorageService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/collector")
public class DataCollectorController {
    private final LogStorageService logStorageService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    @PostMapping(value = "/{deviceId}", consumes = {MediaType.TEXT_PLAIN_VALUE})
    public void storeLog(@PathVariable String deviceId, @RequestBody String payload){
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        final byte[] decompress = Zipper.decompress(bytes);
        final ObjectReader objectReader = objectMapper.readerForListOf(Log.class);
        final List<Log> logs = objectReader.readValue(decompress);

        logStorageService.save(deviceId, logs);
    }
    //public void storeLog(@PathVariable String deviceId, @RequestBody byte[] payload) {

//    }
}
