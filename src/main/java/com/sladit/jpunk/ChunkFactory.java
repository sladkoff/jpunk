package com.sladit.jpunk;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ChunkFactory {

    private static Class<? extends Chunk> getChunkClassForType(String type) {
        if (type.equals(Chunk.LinkChunk.CHUNK_TYPE)) {
            return Chunk.LinkChunk.class;
        }
        if (type.equals(Chunk.PunkChunk.CHUNK_TYPE)) {
            return Chunk.PunkChunk.class;
        }
        return Chunk.class;
    }

    private final String type;
    private final Class<? extends Chunk> clazz;

    private ChunkFactory(String type, Class<? extends Chunk> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    <T extends Chunk> T getInstance(byte[] content, byte[] crc) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor(String.class, byte[].class, byte[].class);
            return (T) constructor.newInstance(type, content, crc);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    static ChunkFactory forType(String type) {
        Class<? extends Chunk> chunkClassForType = getChunkClassForType(type);
        return new ChunkFactory(type, chunkClassForType);
    }
}
