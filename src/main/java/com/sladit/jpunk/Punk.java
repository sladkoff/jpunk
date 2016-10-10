package com.sladit.jpunk;

import com.sun.istack.internal.Nullable;
import org.bouncycastle.openpgp.PGPException;
import org.c02e.jpgpj.Decryptor;
import org.c02e.jpgpj.Encryptor;
import org.c02e.jpgpj.HashingAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.zip.CRC32;

public class Punk {

    private static final String PUNK_CHUNK_TYPE = "PUNK";
    private static final byte[] PNG_START_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    public static byte[] encode(byte[] target, byte[] payload, EncryptionConfig encryptionConfig) throws IOException,
                                                                                                         PGPException {

        if (encryptionConfig != null) {
            Encryptor encryptor = new Encryptor(encryptionConfig.getKeyRing());
            if (!encryptionConfig.isUnsigned()) {
                encryptor.setSigningAlgorithm(HashingAlgorithm.Unsigned);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            encryptor.encrypt(new ByteArrayInputStream(payload), out);
            payload = out.toByteArray();
        }

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

    public static byte[] encode(byte[] target, byte[] payload) throws IOException, PGPException {
        return encode(target, payload, null);
    }

    public static byte[] decode(byte[] source, @Nullable EncryptionConfig encryptionConfig) throws IOException,
                                                                                                   PGPException {
        ChunkReader chunkReader = new ChunkReader(new ByteArrayInputStream(source));
        Optional<Chunk> chunkByType = chunkReader.findChunkByType(PUNK_CHUNK_TYPE);
        if (chunkByType.isPresent()) {
            byte[] content = chunkByType.get().getContent();
            if (encryptionConfig != null) {
                Decryptor decryptor = new Decryptor(encryptionConfig.getKeyRing());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                decryptor.decrypt(new ByteArrayInputStream(content), out);
                return out.toByteArray();
            }
            return content;
        }

        return null;
    }

    public static byte[] decode(byte[] source) throws IOException, PGPException {
        return decode(source, null);
    }

}
