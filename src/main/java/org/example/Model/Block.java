package org.example.Model;

import org.example.Interface.IBlock;
import org.example.Interface.ITransaction;
import org.example.Merkle.MerkleHash;
import org.example.Merkle.MerkleTree;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.*;

import static org.example.Storage.Storage.readFromFile;

public class Block implements IBlock {
    private List<ITransaction> transaction;
    // private long timestamp;
    // Set as part of the block creation process.
    private int blockNumber;
    private LocalDate createdDate;
    private String blockHash;
    private String previousBlockHash;
    private IBlock nextBlock;
    private MerkleTree merkleTree = new MerkleTree();
    private String merkleRootHash;

    public Block(int blockNumber) {
        this.blockNumber = blockNumber;
        this.createdDate = LocalDate.now(); //
        this.transaction = new ArrayList<ITransaction>();
    }

    @Override
    public List<ITransaction> getTransaction() {
        return transaction;
    }

    @Override
    public IBlock getNextBlock() {
        return nextBlock;
    }

    @Override
    public void setNextBlock(IBlock nextBlock) {
        this.nextBlock = nextBlock;
    }

    @Override
    public boolean isValidChain(String prevBlockHash, boolean verbose) {
        // Gọi phương thức mới với các tham số giả
        return isValidChainDetailed(prevBlockHash, verbose, new ArrayList<>(), new HashMap<>());
    }

    @Override
    public String getMerkleRootHash() {
        if (merkleRootHash != null && !merkleRootHash.isEmpty()) {
            return merkleRootHash;
        }
        // Xây dựng cây Merkle từ các giao dịch trong block
        buildMerkleTree();

        // Kiểm tra xem cây Merkle đã được xây dựng chưa
        if (merkleTree.getRootNode() != null) {
            // Trả về hash gốc dưới dạng chuỗi
            return merkleTree.getRootNode().getHash().toString();
        }

        // Trả về chuỗi rỗng nếu không có giao dịch hoặc không thể xây dựng cây
        return "";
    }

    @Override
    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    @Override
    public void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }

    @Override
    public void addTransaction(ITransaction transaction) {
        if (this.transaction.size() < 4) {
            this.transaction.add(transaction);
        } else {
            System.out.println("Block already contains maximum of 2 transactions");
        }
    }

    @Override
    public String calculateBlockHash(String previousBlockHash) {
        try {
            String blockHeader = blockNumber + createdDate.toString() + previousBlockHash;
            String combined = merkleTree.getRootNode() + blockHeader;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error calculating block hash", e);
        }
    }

    @Override
    public void setBlockHash(IBlock parent) {
        if (parent != null) {
            previousBlockHash = parent.getBlockHash();
            parent.setNextBlock(this);
        } else {
            // Previous block is the genesis block.
            previousBlockHash = null;
        }

        buildMerkleTree();

        blockHash = calculateBlockHash(previousBlockHash);
    }

    private void buildMerkleTree() {
        merkleTree = new MerkleTree();

        for (ITransaction txn : transaction) {
            merkleTree.appendLeaf(MerkleHash.create(txn.caCalculateTransactionHash()));
        }

        merkleTree.buildTree();
    }

    @Override
    public String getBlockHash() {
        return blockHash;
    }

    @Override
    public LocalDate getCreatedDate() {
        return createdDate;
    }

    @Override
    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int getBlockNumber() {
        return blockNumber;
    }

    public boolean isValidChainDetailed(String prevBlockHash, boolean verbose,
            List<IBlock> invalidBlocks,
            Map<Integer, List<Map<String, String>>> invalidTransactions) {
        boolean isValid = true;
        boolean blockValid = true;

//        // Xây dựng lại cây Merkle từ các giao dịch hiện tại
//        buildMerkleTree();
//
//        // Kiểm tra các giao dịch trong block
//        List<Map<String, String>> txnInfoList = new ArrayList<>();
//
//        for (ITransaction txn : transaction) {
//            Transaction transaction = (Transaction) txn;
//            String storedHash = transaction.caCalculateTransactionHash();
//
//            // So sánh các thông tin cụ thể để xác định thay đổi, bỏ qua timestamp và chữ ký
//            String originalData = transaction.getUnitName() +
//                    transaction.getCodeProduct() +
//                    transaction.getNameProduct() +
//                    transaction.getDateStart() +
//                    transaction.getDateEnd();
//
//            // Tạo bản sao giữ nguyên dữ liệu gốc (không tạo timestamp mới)
//            Transaction clonedTransaction = new Transaction(
//                    transaction.getUnitName(),
//                    transaction.getCodeProduct(),
//                    transaction.getNameProduct(),
//                    transaction.getDateStart(),
//                    transaction.getDateEnd(),
//                    transaction.getTimestamp() // Sử dụng timestamp gốc, cần thêm getter
//            );
//
//            String clonedData = clonedTransaction.getUnitName() +
//                    clonedTransaction.getCodeProduct() +
//                    clonedTransaction.getNameProduct() +
//                    clonedTransaction.getDateStart() +
//                    clonedTransaction.getDateEnd();
//
//            // So sánh dữ liệu thô thay vì hash
//            if (!originalData.equals(clonedData)) {
//                blockValid = false;
//
//                // Lưu thông tin về giao dịch không hợp lệ
//                Map<String, String> txnInfo = new HashMap<>();
//                txnInfo.put("nameProduct", transaction.getNameProduct());
//                txnInfo.put("codeProduct", transaction.getCodeProduct());
//                txnInfo.put("unitName", transaction.getUnitName());
//                txnInfo.put("dateStart", transaction.getDateStart().toString());
//                txnInfo.put("dateEnd", transaction.getDateEnd().toString());
//                txnInfo.put("storedHash", storedHash);
//                txnInfo.put("calculatedHash", clonedTransaction.caCalculateTransactionHash());
//
//                txnInfoList.add(txnInfo);
//            }
//        }

        // Kiểm tra hash của block
        // Xây dựng lại cây Merkle từ các giao dịch hiện tại
        buildMerkleTree();

        // Đọc blockchain từ file
        BlockChain fileBlockchain = org.example.Storage.Storage.readFromFile("blockchain.txt");

        // Kiểm tra các giao dịch trong block
        List<Map<String, String>> txnInfoList = new ArrayList<>();

        if (fileBlockchain != null) {
            // Tìm block tương ứng trong file
            Block fileBlock = null;
            for (IBlock block : fileBlockchain.getBlocks()) {
                if (block.getBlockNumber() == this.blockNumber) {
                    fileBlock = (Block) block;
                    break;
                }
            }

            if (fileBlock != null) {
                List<ITransaction> fileTransactions = fileBlock.getTransaction();

                // So sánh từng giao dịch trong block hiện tại với giao dịch trong file
                for (int i = 0; i < transaction.size() && i < fileTransactions.size(); i++) {
                    Transaction memoryTxn = (Transaction) transaction.get(i);
                    Transaction fileTxn = (Transaction) fileTransactions.get(i);

                    // So sánh các trường dữ liệu
                    if (!memoryTxn.getUnitName().equals(fileTxn.getUnitName()) ||
                            !memoryTxn.getCodeProduct().equals(fileTxn.getCodeProduct()) ||
                            !memoryTxn.getNameProduct().equals(fileTxn.getNameProduct()) ||
                            !memoryTxn.getDateStart().toString().equals(fileTxn.getDateStart().toString()) ||
                            !memoryTxn.getDateEnd().toString().equals(fileTxn.getDateEnd().toString())) {

                        blockValid = false;

                        // Lưu thông tin về giao dịch đã thay đổi
                        Map<String, String> txnInfo = new HashMap<>();
                        txnInfo.put("unitNameFile", fileTxn.getUnitName());
                        txnInfo.put("unitNameMemory", memoryTxn.getUnitName());
                        txnInfo.put("codeProductFile", fileTxn.getCodeProduct());
                        txnInfo.put("codeProductMemory", memoryTxn.getCodeProduct());
                        txnInfo.put("nameProductFile", fileTxn.getNameProduct());
                        txnInfo.put("nameProductMemory", memoryTxn.getNameProduct());
                        txnInfo.put("dateStartFile", fileTxn.getDateStart().toString());
                        txnInfo.put("dateStartMemory", memoryTxn.getDateStart().toString());
                        txnInfo.put("dateEndFile", fileTxn.getDateEnd().toString());
                        txnInfo.put("dateEndMemory", memoryTxn.getDateEnd().toString());
                        txnInfo.put("hashFile", fileTxn.caCalculateTransactionHash());
                        txnInfo.put("hashMemory", memoryTxn.caCalculateTransactionHash());

                        txnInfoList.add(txnInfo);
                    }
                }
            }
        }

        String newBlockHash = calculateBlockHash(prevBlockHash);
        if (!newBlockHash.equals(blockHash)) {
            blockValid = false;
        } else {
            // Kiểm tra previous hash có khớp không
            boolean previousHashValid = (previousBlockHash == null && prevBlockHash == null) ||
                    (previousBlockHash != null && previousBlockHash.equals(prevBlockHash));
            if (!previousHashValid) {
                blockValid = false;
            }
        }

        if (!blockValid) {
            isValid = false;
            invalidBlocks.add(this);
            invalidTransactions.put(blockNumber, txnInfoList);
        }

        if (verbose) {
            if (!blockValid) {
                System.out.println("Block Number " + blockNumber + " : FAILED VERIFICATION");
            } else {
                System.out.println("Block Number " + blockNumber + " : PASS VERIFICATION");
            }
        }

        // Kiểm tra block tiếp theo
        if (nextBlock != null) {
            boolean nextBlockValid = ((Block) nextBlock).isValidChainDetailed(newBlockHash, verbose, invalidBlocks,
                    invalidTransactions);
            return isValid && nextBlockValid;
        }

        return isValid;
    }
}
