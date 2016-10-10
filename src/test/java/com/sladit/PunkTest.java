package com.sladit;

import com.sladit.jpunk.EncryptionConfig;
import com.sladit.jpunk.Punk;
import org.bouncycastle.openpgp.PGPException;
import org.c02e.jpgpj.Key;
import org.c02e.jpgpj.Ring;
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

    private Key secretKeySender;
    private Key publicKeySender;
    private Key secretKeyReceiver;
    private Key publicKeyReceiver;

    @Before
    public void setup() throws IOException {
        File inputFile = new File("src/test/resources/PNG_transparency_demonstration_1.png");

        try {
            secretKeySender = new Key(new File("src/test/resources/punk.key"), "12345678");
            publicKeySender = new Key(new File("src/test/resources/punk.gpg"));

            secretKeyReceiver = new Key(new File("src/test/resources/punk2.key"), "12345678");
            publicKeyReceiver = new Key(new File("src/test/resources/punk2.gpg"));

        } catch (PGPException e) {
            e.printStackTrace();
        }

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

    @Test
    public void decodeString() throws IOException, PGPException {
        String payload = STRING_PAYLOAD;
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes());

        byte[] decoded = Punk.decode(steganogram);
        assertArrayEquals(STRING_PAYLOAD.getBytes(), decoded);
        assertEquals(STRING_PAYLOAD, new String(decoded));
    }

    @Test
    public void decodeEncryptedString() throws IOException, PGPException {
        String payload = STRING_PAYLOAD;

        EncryptionConfig encryptionConfig = new EncryptionConfig();
        encryptionConfig.setUnsigned(true);
        encryptionConfig.setKeyRing(new Ring(secretKeySender, publicKeyReceiver));
        byte[] steganogram = Punk.encode(inputBytes, payload.getBytes(), encryptionConfig);

        encryptionConfig.setKeyRing(new Ring(publicKeySender, secretKeyReceiver));
        byte[] decoded = Punk.decode(steganogram, encryptionConfig);
        assertArrayEquals(STRING_PAYLOAD.getBytes(), decoded);
        assertEquals(STRING_PAYLOAD, new String(decoded));
    }

}
