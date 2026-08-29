package com.fittrack.support;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/** Builds a real, byte-valid PNG so upload validation is exercised rather than bypassed. */
public final class TestImages {

    private TestImages() {}

    public static byte[] onePixelPng() {
        byte[] raw = new byte[] {0, (byte) 255, 0, 0}; // one filter byte + one RGB pixel
        byte[] compressed = deflate(raw);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
        writeChunk(out, "IHDR", ihdr());
        writeChunk(out, "IDAT", compressed);
        writeChunk(out, "IEND", new byte[0]);
        return out.toByteArray();
    }

    private static byte[] ihdr() {
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        writeInt(header, 1); // width
        writeInt(header, 1); // height
        header.write(8); // bit depth
        header.write(2); // colour type: truecolour
        header.write(0); // compression
        header.write(0); // filter
        header.write(0); // interlace
        return header.toByteArray();
    }

    private static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        while (!deflater.finished()) {
            out.write(buffer, 0, deflater.deflate(buffer));
        }
        deflater.end();
        return out.toByteArray();
    }

    private static void writeChunk(ByteArrayOutputStream out, String type, byte[] data) {
        writeInt(out, data.length);
        byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        write(out, typeBytes);
        write(out, data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        writeInt(out, (int) crc.getValue());
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private static void write(ByteArrayOutputStream out, byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
