package com.sladit.jpunk;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;

class Chunk {

    static final String END_CHUNK_TYPE = "IEND";

    private final String type;
    private final byte[] content;
    private final byte[] crc;

    Chunk(String type, byte[] content, byte[] crc) {
        this.type = type;
        this.content = content;
        this.crc = crc;
    }

    Chunk(String type, byte[] content, CRC32 crc) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt((int) crc.getValue());
        this.type = type;
        this.content = content;
        this.crc = buffer.array();
    }

    int getSize() {
        return content.length;
    }

    String getType() {
        return type;
    }

    byte[] getContent() {
        return content;
    }

    public byte[] getCrc() {
        return crc;
    }

    private String getCrcHex() {
        return DatatypeConverter.printHexBinary(crc);
    }

    byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream(getSize() + 12)) {
            os.write(ByteBuffer.allocate(4).putInt(getSize()).array());
            os.write(type.getBytes());
            os.write(content);
            os.write(crc);
            return os.toByteArray();
        }
    }

    @Override
    public String toString() {
        return type + ": " + getCrcHex();
    }

}
