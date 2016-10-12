package com.sladit.jpunk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.CRC32;

public class Punk {

    private static final byte[] PNG_START_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public static byte[] encode(byte[] target, byte[] payload) throws IOException {

        ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(target));

        List<Chunk> chunks = chunkReader.readChunks();

        Chunk punk = new Chunk(Chunk.PunkChunk.CHUNK_TYPE, payload, calculateCrcForPayload(payload));

        chunks.add(chunks.size() - 1, punk);

        ByteArrayOutputStream os = new ByteArrayOutputStream(target.length + punk.getSize() + 8);
        os.write(PNG_START_SIGNATURE);
        for (Chunk chunk : chunks) {
            os.write(chunk.getBytes());
        }
        return os.toByteArray();
    }

    public static List<byte[]> encode(byte[][] targets, byte[] payload) throws IOException, NoSuchAlgorithmException {

        List<byte[]> encodedFiles = new ArrayList<>();
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] payloadMd5 = messageDigest.digest(payload);

        int chunkSize = (int) Math.ceil((double) payload.length / targets.length);

        int start = 0;
        for (byte[] target : targets) {
            int end = start + chunkSize > payload.length ? payload.length : start + chunkSize;
            byte[] chunkBytes = Arrays.copyOfRange(payload, start, end);
            start += chunkSize;

            ByteBuffer buffer = ByteBuffer.allocate(4 + payloadMd5.length);
            buffer.put((byte) start);
            buffer.put(payloadMd5);
            byte[] linkData = buffer.array();

            ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(target));
            List<Chunk> chunks = chunkReader.readChunks();

            Chunk link = new Chunk(Chunk.LinkChunk.CHUNK_TYPE, linkData, calculateCrcForPayload(linkData));
            Chunk punk = new Chunk(Chunk.PunkChunk.CHUNK_TYPE, chunkBytes, calculateCrcForPayload(chunkBytes));

            chunks.add(chunks.size() - 1, link);
            chunks.add(chunks.size() - 1, punk);

            int encodedLength = target.length + punk.getSize() + link.getSize() + 8;

            ByteArrayOutputStream os = new ByteArrayOutputStream(encodedLength);
            os.write(PNG_START_SIGNATURE);
            for (Chunk chunk : chunks) {
                os.write(chunk.getBytes());
            }
            encodedFiles.add(os.toByteArray());

        }

        return encodedFiles;
    }

    public static byte[] decode(byte[] source) throws IOException {
        ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(source));
        Optional<Chunk> chunkByType = chunkReader.findChunkByType(Chunk.PunkChunk.CHUNK_TYPE);
        if (chunkByType.isPresent()) {
            return chunkByType.get().getContent();
        }

        return null;
    }

    public static byte[] decode(List<byte[]> sources) throws IOException {
        Map<Chunk.LinkChunk, Chunk.PunkChunk> chunks = new TreeMap<>();

        for (byte[] source : sources) {
            ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(source));
            Chunk.LinkChunk link = (Chunk.LinkChunk) chunkReader.findChunkByType(Chunk.LinkChunk.CHUNK_TYPE).orElse(null);
            Chunk.PunkChunk punk = (Chunk.PunkChunk) chunkReader.findChunkByType(Chunk.PunkChunk.CHUNK_TYPE).orElse(null);
            if (link == null || punk == null) continue;
            chunks.put(link, punk);
        }

        byte[] checksum = null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for(Map.Entry<Chunk.LinkChunk, Chunk.PunkChunk> entry : chunks.entrySet()) {
            os.write(entry.getValue().getContent());
            if (checksum != null) {
                if (!Arrays.equals(checksum, entry.getKey().getChecksum())) throw new RuntimeException("Checksum not equal");
            }
            checksum = entry.getKey().getChecksum();
        }

        return os.toByteArray();
    }

    private static CRC32 calculateCrcForPayload(byte[] payload) {
        CRC32 crc = new CRC32();
        crc.update(payload);
        return crc;
    }

}
