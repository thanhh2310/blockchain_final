package org.example.Merkle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MerkleNode implements Iterable<MerkleNode> {
    private MerkleHash hash;
    private MerkleNode leftNode;
    private MerkleNode rightNode;
    private MerkleNode parent;

    public MerkleNode() {
    }

    /**
     * Hàm khởi tạo cho node cơ sở (lá), đại diện cho cấp thấp nhất của cây.
     */
    public MerkleNode(MerkleHash hash) {
        this.hash = hash;
    }

    /**
     * Hàm khởi tạo cho node cha.
     */
    public MerkleNode(MerkleNode left, MerkleNode right) {
        this.leftNode = left;
        this.rightNode = right;
        this.leftNode.parent = this;
        if (right != null) {
            this.rightNode.parent = this;
        }
        computeHash();
    }

    public MerkleHash getHash() {
        return hash;
    }

    public MerkleNode getLeftNode() {
        return leftNode;
    }

    public MerkleNode getRightNode() {
        return rightNode;
    }

    public MerkleNode getParent() {
        return parent;
    }

    public boolean isLeaf() {
        return leftNode == null && rightNode == null;
    }

    @Override
    public String toString() {
        return hash.toString();
    }

    @Override
    public Iterator<MerkleNode> iterator() {
        return iterate(this).iterator();
    }

    /**
     * Duyệt cây từ dưới lên/trái sang phải.
     */
    protected List<MerkleNode> iterate(MerkleNode node) {
        List<MerkleNode> nodes = new ArrayList<>();

        if (node.leftNode != null) {
            nodes.addAll(iterate(node.leftNode));
        }

        if (node.rightNode != null) {
            nodes.addAll(iterate(node.rightNode));
        }

        nodes.add(node);
        return nodes;
    }

    public List<MerkleNode> leaves() {
        return StreamSupport.stream(this.spliterator(), false)
                .filter(n -> n.leftNode == null && n.rightNode == null)
                .collect(Collectors.toList());
    }

    protected void computeHash() {
        // Cách thực hiện thay thế - không lặp lại node trái, mà mang hash của node trái lên.
        if (rightNode == null) {
            hash = leftNode.hash;
        } else {
            byte[] combined = concatBytes(leftNode.hash.getValue(), rightNode.hash.getValue());
            hash = MerkleHash.create(combined);
        }

        if (parent != null) {
            parent.computeHash(); // Đệ quy, vì hash của chúng ta đã thay đổi.
        }
    }

    // Phương thức tiện ích để nối các mảng byte (tương tự như LINQ's Concat trong C#)
    private byte[] concatBytes(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public MerkleNode getSibling() {
        if (this.parent == null) {
            return null; // root node không có sibling
        }

        if (this.parent.getLeftNode() == this) {
            return this.parent.getRightNode();
        } else {
            return this.parent.getLeftNode();
        }
    }

}
