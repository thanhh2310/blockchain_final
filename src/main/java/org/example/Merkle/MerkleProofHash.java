package org.example.Merkle;

public class MerkleProofHash {
    public enum Branch {
        LEFT,
        RIGHT,
        OLD_ROOT,
        AUDIT
    }

    private MerkleHash hash;
    private Branch direction;

    public MerkleProofHash(MerkleHash hash, Branch direction) {
        this.hash = hash;
        this.direction = direction;
    }

    public MerkleHash getHash() {
        return hash;
    }

    public Branch getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return hash.toString();
    }
}
