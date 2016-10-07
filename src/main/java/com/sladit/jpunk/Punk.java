package com.sladit.jpunk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.CRC32;

public class Punk {

    private static final String PUNK_CHUNK_TYPE = "PUNK";
    private static final byte[] PNG_START_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public static byte[] encode(byte[] target, byte[] payload) throws IOException {

        ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(target));

        List<Chunk> chunks = chunkReader.readChunks();

        CRC32 crc = new CRC32();
        crc.update(payload);

        Chunk punk = new Chunk(PUNK_CHUNK_TYPE, payload, crc);

        chunks.add(chunks.size() - 1, punk);

        try (ByteArrayOutputStream os = new ByteArrayOutputStream(target.length + punk.getSize() + 20)) {
            os.write(PNG_START_SIGNATURE);
            for (Chunk chunk : chunks) {
                os.write(chunk.getBytes());
            }
            return os.toByteArray();
        }
    }

    public static byte[] decode(byte[] source) throws IOException {
        ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(source));
        Optional<Chunk> chunkByType = chunkReader.findChunkByType(PUNK_CHUNK_TYPE);
        if (chunkByType.isPresent()) {
            return chunkByType.get().getContent();
        }

        return null;
    }

}
