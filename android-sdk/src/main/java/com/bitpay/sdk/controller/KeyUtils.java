package com.bitpay.sdk.controller;

import com.google.bitcoin.core.Base58;
import com.google.bitcoin.core.ECKey;
import com.google.bitcoin.core.ECKey.ECDSASignature;
import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Utils;

import java.io.IOException;
import java.math.BigInteger;

public class KeyUtils {
	
	final private static char[] hexArray = "0123456789abcdef".toCharArray();
	
	public KeyUtils() {}

    public static ECKey createEcKey()
    {
        //Default constructor uses SecureRandom numbers.
        return new ECKey();
    }

    public static ECKey loadEcKey(String string) throws IOException
    {
        ECKey key = ECKey.fromASN1(hexToBytes(string));
        return key;
    }

    public static ECKey loadFromHexaEncodedPrivateKey(String key) {
        return new ECKey(new BigInteger(key, 16));
    }
    public static String exportPrivateKeyToHexa(ECKey key) {
        return bytesToHex(key.getPrivKeyBytes());
    }

    public static String exportEcKey(ECKey ecKey) throws IOException {
        return bytesToHex(ecKey.toASN1());
    }
    
    public static String deriveSIN(ECKey ecKey) throws IllegalArgumentException
    {
        // Get sha256 hash and then the RIPEMD-160 hash of the public key (this call gets the result in one step).
        byte[] pubKeyHash = ecKey.getPubKeyHash(); 

        // Convert binary pubKeyHash, SINtype and version to Hex
        String version = "0F";
        String SINtype = "02";
        String pubKeyHashHex = bytesToHex(pubKeyHash);

        // Concatenate all three elements
        String preSIN = version + SINtype + pubKeyHashHex;

        // Convert the hex string back to binary and double sha256 hash it leaving in binary both times
        byte[] preSINbyte = hexToBytes(preSIN);
        byte[] hash2Bytes = Utils.doubleDigest(preSINbyte);

        // Convert back to hex and take first four bytes
        String hashString = bytesToHex(hash2Bytes);
        String first4Bytes = hashString.substring(0, 8);

        // Append first four bytes to fully appended SIN string
        String unencoded = preSIN + first4Bytes;
        byte[] unencodedBytes = new BigInteger(unencoded, 16).toByteArray();
        String encoded = Base58.encode(unencodedBytes);

        return encoded;
    }
    	
	public static String sign(ECKey key, String input) {
		byte[] data = input.getBytes();
        Sha256Hash hash = Sha256Hash.create(data);
        ECDSASignature sig = key.sign(hash, null);
        byte[] bytes = sig.encodeToDER();
        return bytesToHex(bytes);
	}
	
    private static int getHexVal(char hex)
    {
        int val = (int)hex;
        return val - (val < 58 ? 48 : (val < 97 ? 55 : 87));
    }

    public static byte[] hexToBytes(String hex) throws IllegalArgumentException
    {
    	char[] hexArray = hex.toCharArray();
    	
        if (hex.length() % 2 == 1)
        {
            throw new IllegalArgumentException("Error: The binary key cannot have an odd number of digits");
        }
        byte[] arr = new byte[hex.length() >> 1];

        for (int i = 0; i < hex.length() >> 1; ++i)
        {
            arr[i] = (byte)((getHexVal(hexArray[i << 1]) << 4) + (getHexVal(hexArray[(i << 1) + 1])));
        }
        return arr;
    }

	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
}
