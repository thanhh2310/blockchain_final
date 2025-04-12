package org.example.Merkle;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MerkleHash {
    private byte[] value;
    private static final int Constants = 32;

    protected MerkleHash() {
    }

    public static MerkleHash create(byte[] buffer) {
        MerkleHash hash = new MerkleHash();
        hash.computeHash(buffer);
        return hash;
    }

    public static MerkleHash create(String buffer) {
        return create(buffer.getBytes(StandardCharsets.UTF_8));
    }

    public static MerkleHash create(MerkleHash left, MerkleHash right) {
        byte[] combined = concatBytes(left.value, right.value);
        return create(combined);
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        MerkleTree.contract(obj instanceof MerkleHash, "rvalue is not a MerkleHash");
        return equals((MerkleHash) obj);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : value) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public void computeHash(byte[] buffer) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            setHash(digest.digest(buffer));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    public void setHash(byte[] hash) {
        MerkleTree.contract(hash.length == Constants, "Unexpected hash length.");
        value = hash;
    }

    public boolean equals(byte[] hash) {
        return Arrays.equals(value, hash);
    }

    public boolean equals(MerkleHash hash) {
        boolean result = false;

        if (hash != null) {
            result = Arrays.equals(value, hash.value);
        }

        return result;
    }

    // Utility method to concatenate byte arrays
    private static byte[] concatBytes(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
