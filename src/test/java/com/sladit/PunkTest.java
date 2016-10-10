package com.sladit;

import com.sladit.jpunk.Punk;
import org.bouncycastle.openpgp.PGPException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

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
    public void encodeString() throws IOException, PGPException {
        String payload = STRING_PAYLOAD;
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());
        assertFalse(Arrays.equals(inputBytes, steganogram));
    }

    @Test
    public void encodeEmptyString() throws IOException, PGPException {
        String payload = "";
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());
        assertFalse(Arrays.equals(inputBytes, steganogram));
    }

    /*@Test
    public void encodeAndEncryptString() throws IOException {
        String payload = STRING_PAYLOAD;
        String passphrase = "Pass@Phrase123";
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes(), passphrase);
        assertFalse(Arrays.equals(inputBytes, steganogram));
    }*/

    @Test
    public void decodeString() throws IOException, PGPException {
        String payload = STRING_PAYLOAD;
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());

        byte[] decoded = Punk.decode(steganogram);
        assertArrayEquals(STRING_PAYLOAD.getBytes(), decoded);
        assertEquals(STRING_PAYLOAD, new String(decoded));
    }

    /*@Test
    public void decodeEncryptedString() throws IOException {
        String payload = STRING_PAYLOAD;
        String passphrase = "Pass@Phrase123";

        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes(), passphrase);

        byte[] decoded = Punk.decode(steganogram, passphrase);
        assertArrayEquals(STRING_PAYLOAD.getBytes(), decoded);
        assertEquals(STRING_PAYLOAD, new String(decoded));
    }*/
}
