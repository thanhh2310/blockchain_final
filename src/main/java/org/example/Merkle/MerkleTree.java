package org.example.Merkle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MerkleTree {
    private MerkleNode rootNode;
    private List<MerkleNode> nodes;
    private List<MerkleNode> leaves;

    public static void contract(boolean condition, String msg) {
        if (!condition) {
            System.out.println(msg);
        }
    }

    public MerkleTree() {
        nodes = new ArrayList<>();
        leaves = new ArrayList<>();
    }

    public MerkleNode getRootNode() {
        return rootNode;
    }

    public MerkleNode appendLeaf(MerkleNode node) {
        nodes.add(node);
        leaves.add(node);

        return node;
    }

    //Thêm một leaf node mới với giá trị hash cụ thể vào cây.
    public MerkleNode appendLeaf(MerkleHash hash) {
        MerkleNode node = createNode(hash);
        nodes.add(node);
        leaves.add(node);

        return node;
    }

    public void appendLeaves(MerkleNode[] nodes) {
        for (MerkleNode node : nodes) {
            appendLeaf(node);
        }
    }

    //Thêm nhiều leaf nodes vào cây cùng lúc từ mảng các hash.
    public List<MerkleNode> appendLeaves(MerkleHash[] hashes) {
        List<MerkleNode> nodes = new ArrayList<>();
        for (MerkleHash hash : hashes) {
            nodes.add(appendLeaf(hash));
        }

        return nodes;
    }

    // Xây dựng cây Merkle từ các leaf đã được thêm, trả về root hash.
    public MerkleHash buildTree() {
        contract(leaves.size() > 0, "Cannot build a tree with no leaves.");
        buildTree(leaves);

        return rootNode.getHash();
    }

    protected void buildTree(List<MerkleNode> nodes) {
        contract(nodes.size() > 0, "node list not expected to be empty.");

        if (nodes.size() == 1) {
            rootNode = nodes.get(0);
        } else {
            List<MerkleNode> parents = new ArrayList<>();

            for (int i = 0; i < nodes.size(); i += 2) {
                MerkleNode right = (i + 1 < nodes.size()) ? nodes.get(i + 1) : null;
                // Constructing the MerkleNode resolves the right node being null.
                MerkleNode parent = createNode(nodes.get(i), right);
                parents.add(parent);
            }

            buildTree(parents);
        }
    }

    //Tạo hash mới từ hai node con trái và phải.
    public static MerkleHash computeHash(MerkleHash left, MerkleHash right) {
        byte[] combined = concatBytes(left.getValue(), right.getValue());
        return MerkleHash.create(combined);
    }

    //Tạo audit proof (chuỗi hash để chứng minh một leaf thuộc cây).
    public List<MerkleProofHash> auditProof(MerkleHash leafHash) {
        List<MerkleProofHash> auditTrail = new ArrayList<>();

        MerkleNode leafNode = findLeaf(leafHash);

        if (leafNode != null) {
            contract(leafNode.getParent() != null, "Expected leaf to have a parent.");
            MerkleNode parent = leafNode.getParent();
            buildAuditTrail(auditTrail, parent, leafNode);
        }

        return auditTrail;
    }

    //  Xác minh tính đúng đắn của một leaf hash có thuộc cây với root đã biết không.
    public static boolean verifyAudit(MerkleHash rootHash, MerkleHash leafHash, List<MerkleProofHash> auditTrail) {
        contract(auditTrail.size() > 0, "Audit trail cannot be empty.");
        MerkleHash testHash = leafHash;

        for (MerkleProofHash auditHash : auditTrail) {
            if (auditHash.getDirection() == MerkleProofHash.Branch.LEFT) {
                byte[] combined = concatBytes(testHash.getValue(), auditHash.getHash().getValue());
                testHash = MerkleHash.create(combined);
            } else {
                byte[] combined = concatBytes(auditHash.getHash().getValue(), testHash.getValue());
                testHash = MerkleHash.create(combined);
            }
        }

        return rootHash.equals(testHash);
    }

    //Tìm node tương ứng với một leaf hash.
    protected MerkleNode findLeaf(MerkleHash leafHash) {
        for (MerkleNode leaf : leaves) {
            if (leaf.getHash().equals(leafHash)) {
                return leaf;
            }
        }
        return null;
    }

    //Hỗ trợ nội bộ tạo audit proof bằng cách duyệt ngược từ leaf lên root.
    protected void buildAuditTrail(List<MerkleProofHash> auditTrail, MerkleNode parent, MerkleNode child) {
        if (parent != null) {
            contract(child.getParent() == parent, "Parent of child is not expected parent.");
            MerkleNode nextChild = parent.getLeftNode() == child ? parent.getRightNode() : parent.getLeftNode();
            MerkleProofHash.Branch direction = parent.getLeftNode() == child ? MerkleProofHash.Branch.LEFT
                    : MerkleProofHash.Branch.RIGHT;
            if (nextChild != null) {
                auditTrail.add(new MerkleProofHash(nextChild.getHash(), direction));
            }

            buildAuditTrail(auditTrail, child.getParent().getParent(), child.getParent());
        }
    }

    //Tạo bằng chứng nhất quán
    public List<MerkleProofHash> consistencyProof(int m) {
        List<MerkleProofHash> hashNodes = new ArrayList<>();
        int idx = (int) Math.log(m) / (int) Math.log(2);

        // Get the leftmost node.
        MerkleNode node = leaves.get(0);

        // Traverse up the tree until we get to the node specified by idx.
        while (idx > 0) {
            node = node.getParent();
            --idx;
        }

        int k = node.leaves().size();
        hashNodes.add(new MerkleProofHash(node.getHash(), MerkleProofHash.Branch.OLD_ROOT));

        if (m == k) {
            // Continue with Rule 3 -- the remainder is the audit proof
            MerkleNode current = node;
            while (current.getParent() != null) {
                MerkleNode sibling = current.getSibling();
                if (sibling != null) {
                    hashNodes.add(new MerkleProofHash(sibling.getHash(), MerkleProofHash.Branch.AUDIT));
                }
                current = current.getParent();
            }
        } else {

            MerkleNode sn = node.getParent().getRightNode();
            boolean traverseTree = true;

            while (traverseTree) {
                contract(sn != null, "Sibling node must exist because m != k");
                int sncount = sn.leaves().size();

                if (m - k == sncount) {
                    hashNodes.add(new MerkleProofHash(sn.getHash(), MerkleProofHash.Branch.OLD_ROOT));
                    break;
                }

                if (m - k > sncount) {
                    hashNodes.add(new MerkleProofHash(sn.getHash(), MerkleProofHash.Branch.OLD_ROOT));
                    sn = sn.getParent().getRightNode();
                    k += sncount;
                } else { // (m - k < sncount)
                    sn = sn.getLeftNode();
                }
            }
        }

        // Rule 3: Apply ConsistencyAuditProof below.

        return hashNodes;
    }

    //Xác minh bằng chứng nhất quán
    public static boolean verifyConsistency(MerkleHash oldRootHash, List<MerkleProofHash> proof) {
        MerkleHash hash, lhash, rhash;

        if (proof.size() > 1) {
            lhash = proof.get(proof.size() - 2).getHash();
            int hidx = proof.size() - 1;
            hash = rhash = MerkleTree.computeHash(lhash, proof.get(hidx).getHash());
            hidx -= 2;

            while (hidx >= 0) {
                lhash = proof.get(hidx).getHash();
                hash = rhash = MerkleTree.computeHash(lhash, rhash);
                --hidx;
            }
        } else {
            hash = proof.get(0).getHash();
        }

        return hash.equals(oldRootHash);
    }

    protected MerkleNode createNode(MerkleHash hash) {
        return new MerkleNode(hash);
    }

    protected MerkleNode createNode(MerkleNode left, MerkleNode right) {
        return new MerkleNode(left, right);
    }

    // Utility method to concatenate byte arrays
    private static byte[] concatBytes(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
