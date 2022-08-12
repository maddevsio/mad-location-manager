package com.maddevs.logtransferobject;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class Zipper {
    private Zipper(){
        throw new UnsupportedOperationException("Util static class");
    }

    @SneakyThrows
    public static byte[] compress(byte[] input){
        Deflater compresser = new Deflater();
        compresser.setInput(input);
        compresser.finish();

        byte[] result = new byte[1024];
        int compressedDataLength = 0;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length)){
            while (!compresser.finished()) {
                compressedDataLength = compresser.deflate(result);
                baos.write(result, 0, compressedDataLength);
            }
            compresser.end();
            return baos.toByteArray();
        }
    }

    @SneakyThrows
    public static String decompress(byte[] bytes){
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bytes.length);
        Inflater decompressor = new Inflater();
        try {
            decompressor.setInput(bytes);
            final byte[] buf = new byte[1024];
            while (!decompressor.finished()) {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            }
        } finally {
            decompressor.end();
        }

        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }
}
