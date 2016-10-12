package com.sladit.jpunk;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
        return getContentSize() + 12;
    }

    int getContentSize() {
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
        ByteArrayOutputStream os = new ByteArrayOutputStream(getSize());
        // 4 bytes: size of content
        os.write(ByteBuffer.allocate(4).putInt(getContentSize()).array());
        // 4 bytes: type of chunk
        os.write(type.getBytes());
        // n bytes: content
        os.write(content);
        // 4 bytes: crc
        os.write(crc);
        return os.toByteArray();
    }

    @Override
    public String toString() {
        return type + ": " + getCrcHex();
    }

    public static String getChunkType() {
        return null;
    }

    public static class LinkChunk extends Chunk implements Comparable<LinkChunk> {

        protected static final String CHUNK_TYPE = "liNk";

        LinkChunk(String type, byte[] content, byte[] crc) {
            super(type, content, crc);
        }

        LinkChunk(String type, byte[] content, CRC32 crc) {
            super(type, content, crc);
        }

        Integer getOrder () {
            byte[] index = Arrays.copyOf(getContent(), 4);
            return ByteBuffer.wrap(index).getInt();
        }

        byte[] getChecksum () {
            return Arrays.copyOfRange(getContent(), 5, getContentSize() - 1);
        }

        @Override
        public int compareTo(LinkChunk o) {
            return getOrder().compareTo(o.getOrder());
        }


    }

    public static class PunkChunk extends Chunk {

        protected static final String CHUNK_TYPE = "puNk";

        PunkChunk(String type, byte[] content, byte[] crc) {
            super(type, content, crc);
        }

        PunkChunk(String type, byte[] content, CRC32 crc) {
            super(type, content, crc);
        }
    }

}
