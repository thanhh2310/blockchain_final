package org.example.Storage;

import org.example.Model.BlockChain;
import org.example.Interface.IBlock;
import org.example.Interface.ITransaction;
import org.example.Model.Transaction;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import org.example.Model.Block;

public class Storage {
    // lưu vào file .txt
    public static void saveToFile(BlockChain blockchain, String filePath) {
        try (FileWriter writer = new FileWriter(filePath);
                BufferedWriter bufferedWriter = new BufferedWriter(writer)) {

            // Lưu thông tin block đầu tiên (head block)
            if (blockchain.getHeadBlock() != null) {
                bufferedWriter.write("BLOCKCHAIN DATA:\n");
                bufferedWriter.write("======================\n\n");

                // Duyệt qua tất cả các block trong blockchain
                for (IBlock block : blockchain.getBlocks()) {
                    bufferedWriter.write("Block #" + block.getBlockNumber() + "\n");
                    bufferedWriter.write("MerkleRoot: " + block.getMerkleRootHash() + "\n");
                    bufferedWriter.write("Ngày tạo: " + block.getCreatedDate() + "\n");
                    bufferedWriter.write("Hash: " + block.getBlockHash() + "\n");
                    bufferedWriter.write("Previous Hash: " +
                            (block.getPreviousBlockHash() != null ? block.getPreviousBlockHash()
                                    : "null (Genesis Block)")
                            + "\n");
                    bufferedWriter.write("\nGiao dịch trong block:\n");

                    List<ITransaction> transactions = block.getTransaction();
                    if (transactions.isEmpty()) {
                        bufferedWriter.write("Không có giao dịch\n");
                    } else {
                        for (int i = 0; i < transactions.size(); i++) {
                            ITransaction transaction = transactions.get(i);
                            bufferedWriter.write("\nGiao dịch #" + (i + 1) + ":\n");
                            bufferedWriter
                                    .write("Đơn vị sản xuất: " + ((Transaction) transaction).getUnitName() + "\n");
                            bufferedWriter.write("Mã sản phẩm: " + ((Transaction) transaction).getCodeProduct() + "\n");
                            bufferedWriter
                                    .write("Tên sản phẩm: " + ((Transaction) transaction).getNameProduct() + "\n");
                            bufferedWriter.write("Ngày sản xuất: " + ((Transaction) transaction).getDateStart() + "\n");
                            bufferedWriter.write("Ngày hết hạn: " + ((Transaction) transaction).getDateEnd() + "\n");
                            bufferedWriter.write("Hash giao dịch: " + transaction.caCalculateTransactionHash() + "\n");
                            bufferedWriter.write("Chữ ký số: " + transaction.signTransaction() + "\n");
                        }
                    }
                    bufferedWriter.write("\n======================\n\n");
                }

                bufferedWriter.write("End of Blockchain");
            } else {
                bufferedWriter.write("Blockchain trống, không có dữ liệu để lưu.");
            }

            System.out.println("Đã lưu thành công blockchain vào file: " + filePath);
        } catch (IOException e) {
            System.err.println("Lỗi khi lưu blockchain vào file: " + e.getMessage());
        }
    }

    public static BlockChain readFromFile(String filePath) {
        BlockChain blockchain = new BlockChain();
        blockchain.setBlocks(new ArrayList<>());

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            Block currentBlock = null;
            Transaction currentTransaction = null;
            boolean readingBlockInfo = false;
            boolean readingTransactions = false;

            // Bỏ qua header
            while ((line = reader.readLine()) != null && !line.contains("BLOCKCHAIN DATA")) {
                // Bỏ qua header
            }

            while ((line = reader.readLine()) != null) {
                // Tạo block mới khi gặp định dạng "Block #X"
                if (line.matches("Block #\\d+")) {
                    readingBlockInfo = true;
                    readingTransactions = false;

                    // Hoàn thành block trước đó nếu có
                    if (currentBlock != null) {
                        blockchain.acceptBlock(currentBlock);
                    }

                    // Tạo block mới
                    int blockNumber = Integer.parseInt(line.substring(7));
                    currentBlock = new Block(blockNumber);
                }
                // Đọc thông tin Merkle Root
                else if (readingBlockInfo && line.startsWith("MerkleRoot: ")) {
                    // Không cần xử lý riêng, vì Merkle Root sẽ được tính lại khi build block
                }
                // Đọc thông tin ngày tạo của block
                else if (readingBlockInfo && line.startsWith("Ngày tạo: ")) {
                    String dateStr = line.substring(10);
                    LocalDate date = LocalDate.parse(dateStr);
                    currentBlock.setCreatedDate(date);
                }
                // Đọc hash của block
                else if (readingBlockInfo && line.startsWith("Hash: ")) {
                    String hash = line.substring(6);
                    // Thiết lập hash cho block
                    try {
                        Field field = Block.class.getDeclaredField("blockHash");
                        field.setAccessible(true);
                        field.set(currentBlock, hash);
                    } catch (Exception e) {
                        System.err.println("Không thể thiết lập hash cho block: " + e.getMessage());
                    }
                }
                // Đọc previous hash của block
                else if (readingBlockInfo && line.startsWith("Previous Hash: ")) {
                    String prevHash = line.substring(15);
                    if (prevHash.equals("null (Genesis Block)")) {
                        currentBlock.setPreviousBlockHash(null);
                    } else {
                        currentBlock.setPreviousBlockHash(prevHash);
                    }
                }
                // Bắt đầu đọc các giao dịch
                else if (line.contains("Giao dịch trong block:")) {
                    readingBlockInfo = false;
                    readingTransactions = true;
                }
                // Bỏ qua thông báo "Không có giao dịch"
                else if (line.contains("Không có giao dịch")) {
                    continue;
                }
                // Đọc thông tin giao dịch mới
                else if (readingTransactions && line.matches("Giao dịch #\\d+:")) {
                    // Chuẩn bị cho giao dịch mới
                    currentTransaction = new Transaction(null, null, null, null, null, System.currentTimeMillis());
                }
                // Đọc thông tin đơn vị sản xuất
                else if (readingTransactions && line.trim().startsWith("Đơn vị sản xuất: ")) {
                    String unitName = line.trim().substring(18);
                    currentTransaction.setUnitName(unitName);
                }
                // Đọc thông tin mã sản phẩm
                else if (readingTransactions && line.trim().startsWith("Mã sản phẩm: ")) {
                    String codeProduct = line.trim().substring(14);
                    currentTransaction.setCodeProduct(codeProduct);
                }
                // Đọc thông tin tên sản phẩm
                else if (readingTransactions && line.trim().startsWith("Tên sản phẩm: ")) {
                    String nameProduct = line.trim().substring(15);
                    currentTransaction.setNameProduct(nameProduct);
                }
                // Đọc thông tin ngày sản xuất
                else if (readingTransactions && line.trim().startsWith("Ngày sản xuất: ")) {
                    String dateStr = line.trim().substring(15);
                    LocalDate dateStart = LocalDate.parse(dateStr);
                    currentTransaction.setDateStart(dateStart);
                }
                // Đọc thông tin ngày hết hạn
                else if (readingTransactions && line.trim().startsWith("Ngày hết hạn: ")) {
                    String dateStr = line.trim().substring(14);
                    LocalDate dateEnd = LocalDate.parse(dateStr);
                    currentTransaction.setDateEnd(dateEnd);
                }
                // Thêm giao dịch vào block sau khi đọc xong thông tin
                else if (readingTransactions && line.trim().startsWith("Chữ ký số: ")) {
                    // Có thể lưu chữ ký nếu cần
                    // Sau khi đọc tất cả thông tin về giao dịch, thêm vào block
                    if (currentTransaction != null && currentBlock != null) {
                        currentBlock.addTransaction(currentTransaction);
                        currentTransaction = null; // Reset để sẵn sàng cho giao dịch tiếp theo
                    }
                }
                // Khi gặp dấu hiệu kết thúc blockchain, lưu block cuối cùng
                else if (line.equals("End of Blockchain")) {
                    if (currentBlock != null) {
                        blockchain.acceptBlock(currentBlock);
                    }
                    break;
                }
            }

            // Thiết lập mối quan hệ giữa các block
            if (!blockchain.getBlocks().isEmpty()) {
                // Thiết lập head block
                blockchain.setHeadBlock(blockchain.getBlocks().get(0));

                // Thiết lập current block là block cuối cùng
                blockchain.setCurrentBlock(blockchain.getBlocks().get(blockchain.getBlocks().size() - 1));

                // Thiết lập next block cho từng block
                for (int i = 0; i < blockchain.getBlocks().size() - 1; i++) {
                    blockchain.getBlocks().get(i).setNextBlock(blockchain.getBlocks().get(i + 1));
                }

                System.out.println("Đã đọc thành công blockchain từ file: " + filePath);
                return blockchain;
            } else {
                System.out.println("Không tìm thấy dữ liệu blockchain hợp lệ trong file.");
                return null;
            }
        } catch (IOException e) {
            System.err.println("Lỗi khi đọc blockchain từ file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
