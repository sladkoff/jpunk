package com.sladit.jpunk;

import org.c02e.jpgpj.Ring;

public class EncryptionConfig {
    private boolean unsigned;
    private Ring keyRing;

    public boolean isUnsigned() {
        return unsigned;
    }

    public void setUnsigned(boolean unsigned) {
        this.unsigned = unsigned;
    }

    public Ring getKeyRing() {
        return keyRing;
    }

    public void setKeyRing(Ring keyRing) {
        this.keyRing = keyRing;
    }
}
