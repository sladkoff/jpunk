package com.sladit;

import com.sladit.jpunk.Punk;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PunkTest {

    private static final String STRING_PAYLOAD = "Super secret Über-Payload!";

    private byte[] inputBytes;

    @Before
    public void setup() throws IOException {
        File inputFile = new File("src/test/resources/PNG_transparency_demonstration_1.png");
        inputBytes = Files.readAllBytes(inputFile.toPath());
    }

    @Test
    public void encodeString() throws IOException {
        String payload = STRING_PAYLOAD;
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());
        assertFalse(Arrays.equals(inputBytes, steganogram));
    }

    @Test
    public void encodeEmptyString() throws IOException {
        String payload = "";
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());
        assertFalse(Arrays.equals(inputBytes, steganogram));
    }

    @Test
    public void decodeString() throws IOException {
        String payload = STRING_PAYLOAD;
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());

        byte[] decoded = Punk.decode(steganogram);
        assertArrayEquals(STRING_PAYLOAD.getBytes(), decoded);
        assertEquals(STRING_PAYLOAD, new String(decoded));
    }

    @Test
    public void splitEncodeDecodeString() throws IOException, NoSuchAlgorithmException {
        String payload = "This is a large payload which will be split & attached to multiple files.";

        byte[][] files = new byte[4][inputBytes.length];

        files[0] = inputBytes;
        files[1] = inputBytes;
        files[2] = inputBytes;
        files[3] = inputBytes;

        List<byte[]> steganograms = Punk.encode(files, payload.getBytes());

        steganograms.add(inputBytes);
        steganograms.add(payload.getBytes());
        Collections.shuffle(steganograms);

        byte[] decoded = Punk.decode(steganograms);

        assertEquals(payload, new String(decoded));
    }
}
