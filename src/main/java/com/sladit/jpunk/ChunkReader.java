package com.sladit.jpunk;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ChunkReader {

    private ByteArrayInputStream in;

    ChunkReader(ByteArrayInputStream in) throws IOException {
        this.in = in;
        in.skip(8);
        in.mark(0);
    }

    Chunk readNextChunk() throws IOException {

        byte[] chunkSize = new byte[4];
        in.read(chunkSize, 0, 4);
        int size = ByteBuffer.wrap(chunkSize).getInt();

        byte[] chunkType = new byte[4];
        in.read(chunkType, 0, 4);
        String type = new String(chunkType);

        byte[] chunkContent = new byte[size];
        in.read(chunkContent, 0, size);

        byte[] chunkCrc = new byte[4];
        in.read(chunkCrc, 0, 4);

        return new Chunk(type, chunkContent, chunkCrc);
    }

    Chunk findEndChunk() throws IOException {
        Chunk chunk = readNextChunk();

        while (!Chunk.END_CHUNK_TYPE.equals(chunk.getType())) {
            chunk = readNextChunk();
        }

        return chunk;
    }

    Optional<Chunk> findChunkByType(String type) throws IOException {
        return readChunks().stream().filter(c -> c.getType().equals(type)).findFirst();
    }

    List<Chunk> readChunks() throws IOException {
        List<Chunk> chunks = new ArrayList<>();

        Chunk chunk = readNextChunk();
        chunks.add(chunk);

        while (!Chunk.END_CHUNK_TYPE.equals(chunk.getType())) {
            chunk = readNextChunk();
            chunks.add(chunk);
        }

        return chunks;
    }

}
