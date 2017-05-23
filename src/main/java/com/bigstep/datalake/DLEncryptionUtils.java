package com.bigstep.datalake;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by Serban.Mateescu on 3/20/2017.
 */
public class DLEncryptionUtils {
    private static DLEncryptionUtils instance = null;

    private String strName = "DLENCRYPTION";
    private String strVersion = "1.0";
    private String strCipher = "AES/CTR/NoPadding";
    private final int nHeaderDetailLength = 128;
    private final int nIVLength = 16;
    private final int nAesKeyLength = 16;
    private boolean _isInitialised = false;
    private String _aesKeyPath;
    private byte[] _aesKey;

    private DLEncryptionUtils() {

    }

    public static DLEncryptionUtils getInstance() {
        if (instance == null) {
            instance = new DLEncryptionUtils();
        }
        return instance;
    }

    private SecureRandom secureRandom = null;

    public String getTransform() {
        return strCipher;
    }

    public void loadAesKeyFromStringPath(String aesKeyPath) throws IOException {
        _aesKeyPath = aesKeyPath;

        if (!_isInitialised) {
            File aesKeyFile = new File(_aesKeyPath);

            if (!aesKeyFile.exists())
                throw new IOException("AES key file " + _aesKeyPath + " not found");

            if (!aesKeyFile.canRead())
                throw new IOException("AES key file " + _aesKeyPath + " cannot be accessed");

            File file = new File(_aesKeyPath);
            int size = (int) file.length();

            assert (size == nAesKeyLength);

            _aesKey = new byte[size];
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(_aesKey, 0, _aesKey.length);
            buf.close();

            _isInitialised = true;
        }
        //else {
        //    throw new IOException("DLEncryption already initialized.");
        //}
    }

    public boolean isInitialised() {
        return _isInitialised;
    }

    public int getHeaderDetailLength() {
        return nHeaderDetailLength;
    }

    public int getIVLength() {
        return nIVLength;
    }

    public byte[] getHeaderDetail() throws UnsupportedEncodingException {
        //TODO: change language level to 1.8 so that StringBuilder may be used;
        String headerDetail = strName + ";" + strVersion + ";" + strCipher;
        String headerDetailPadded = String.format("%1$-" + nHeaderDetailLength + "s", headerDetail).replace(' ', '#');
        return headerDetailPadded.getBytes("UTF-8");
    }

    public byte[] getSecretKey() {
        return _aesKey;
    }

    public byte[] generateRandomIV() throws NoSuchAlgorithmException {
        if (secureRandom == null) {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }

        byte[] initVector = new byte[nIVLength];
        secureRandom.nextBytes(initVector);

        return initVector;
    }
}
