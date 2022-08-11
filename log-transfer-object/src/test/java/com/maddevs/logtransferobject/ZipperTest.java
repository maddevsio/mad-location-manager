package com.maddevs.logtransferobject;

import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class ZipperTest {
    private final String original = "[{\"absEastAcceleration\":0.0026241629384458065,\"absNorthAcceleration\":0.002854469232261181," +
            "\"absUpAcceleration\":0.003351937048137188,\"logMessageType\":\"KALMAN_PREDICT\"}," +
            "{\"absEastAcceleration\":0.00262649729847908,\"absNorthAcceleration\":0.002856567734852433,\"absUpAcceleration\":0.003352721920236945,\"logMessageType\":\"KALMAN_PREDICT\"},{\"logMessageType\":\"ABS_ACC_DATA\"},{\"absEastAcceleration\":0.0026288325898349285,\"absNorthAcceleration\":0.0028580527286976576,\"absUpAcceleration\":0.0033526020124554634,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.0026314593851566315,\"absNorthAcceleration\":0.002859854605048895,\"absUpAcceleration\":0.0033534124959260225,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.0026338016614317894,\"absNorthAcceleration\":0.0028616366907954216,\"absUpAcceleration\":0.003353261621668935,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.002636728808283806,\"absNorthAcceleration\":0.002860370557755232,\"absUpAcceleration\":0.003352406434714794,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.002639070153236389,\"absNorthAcceleration\":0.0028631428722292185,\"absUpAcceleration\":0.003354086773470044,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.0026417053304612637,\"absNorthAcceleration\":0.0028633615002036095,\"absUpAcceleration\":0.003353109350427985,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.002644339809194207,\"absNorthAcceleration\":0.002863579895347357,\"absUpAcceleration\":0.0033521309960633516,\"logMessageType\":\"KALMAN_PREDICT\"},{\"absEastAcceleration\":0.0026466818526387215,\"absNorthAcceleration\":0.0028650856111198664,\"absUpAcceleration\":0.0033529598731547594,\"logMessageType\":\"KALMAN_PREDICT\"}]";

    @org.junit.jupiter.api.Test
    void process() {
        String compessedString = zip(original);
        String restored = unzip(compessedString);

        Assertions.assertEquals(original, restored);

    }

    @org.junit.jupiter.api.Test
    void bytesToString() {
        final byte[] bytes = original.getBytes(StandardCharsets.UTF_8);
        final String s = new String(bytes, StandardCharsets.UTF_8);
        final byte[] bytes1 = s.getBytes(StandardCharsets.UTF_8);

        Assertions.assertArrayEquals(bytes, bytes1);
    }

    private String zip(String payload) {
        final byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        final byte[] compressed = Zipper.compress(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private String unzip(String payload) {
        byte[] bytes = Base64.getDecoder().decode(payload);
        final byte[] decompressed = Zipper.decompress(bytes);
        String restored = new String(decompressed, StandardCharsets.UTF_8);
        return restored;
    }
}
