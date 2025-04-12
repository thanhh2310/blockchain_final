package org.example;

import org.example.Interface.IBlock;
import org.example.Interface.ITransaction;
import org.example.Model.Block;
import org.example.Model.BlockChain;
import org.example.Model.Transaction;
import org.example.Storage.Storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class Main {
    // Danh sách các giao dịch đang chờ
    private static ArrayList<Transaction> pendingTransactions = new ArrayList<>();

    public static void main(String[] args) {
        BlockChain blockchain = new BlockChain();
        blockchain.setBlocks(new ArrayList<>()); // Khởi tạo danh sách blocks
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Blockchain Management System ===");
            System.out.println("1. Thêm giao dịch mới");
            System.out.println("2. Tạo block mới từ các giao dịch đang chờ");
            System.out.println("3. Xác minh tính hợp lệ của một giao dịch");
            System.out.println("4. Kiểm tra tính toàn vẹn của blockchain");
            System.out.println("5. Lưu blockchain vào file");
            System.out.println("6. Đọc blockchain từ file");
            System.out.println("7. Hiển thị thông tin blockchain");
            System.out.println("8. Kiểm tra tính toàn vẹn của blockchain từ file");
            System.out.println("9. Thoát");
            System.out.print("Chọn một tùy chọn: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Xóa bộ đệm
            } catch (Exception e) {
                System.out.println("Vui lòng nhập một số từ 1-8");
                scanner.nextLine(); // Xóa bộ đệm
                continue;
            }

            switch (choice) {
                case 1:
                    addNewTransaction(scanner);
                    break;
                case 2:
                    createNewBlock(blockchain);
                    break;
                case 3:
                    verifyTransaction();
                    break;
                case 4:
                    verifyBlockchain(blockchain);
                    break;
                case 5:
                    saveBlockchain(blockchain, scanner);
                    break;
                case 6:
                    BlockChain loadedBlockchain = loadBlockchain(scanner);
                    if (loadedBlockchain != null) {
                        // Thay thế blockchain hiện tại bằng blockchain đã tải
                        blockchain = loadedBlockchain;
                    }
                    break;
                case 7:
                    displayBlockchainInfo(blockchain);
                    break;
                case 8:
                    verifyBlockchainFromFile(scanner);
                    break;
                case 9:
                    System.out.println("Cảm ơn đã sử dụng hệ thống quản lý blockchain!");
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Tùy chọn không hợp lệ. Vui lòng thử lại.");
            }
        }
    }

    private static void verifyBlockchainFromFile(Scanner scanner) {
        System.out.println("\n=== Kiểm Tra Tính Toàn Vẹn Của Blockchain Từ File ===");
        
        // Đọc blockchain từ file
        BlockChain loadedBlockchain = loadBlockchain(scanner);
        
        if (loadedBlockchain != null) {
            // Kiểm tra tính toàn vẹn của blockchain đã tải
            verifyBlockchain(loadedBlockchain);
        }
    }

    private static void addNewTransaction(Scanner scanner) {
        System.out.println("\n=== Thêm Giao Dịch Mới ===");

        System.out.print("Nhập đơn vị sản xuất: ");
        String unitName = scanner.nextLine();

        System.out.print("Nhập mã sản phẩm: ");
        String codeProduct = scanner.nextLine();

        System.out.print("Nhập tên sản phẩm: ");
        String nameProduct = scanner.nextLine();

        LocalDate dateStart = null;
        while (dateStart == null) {
            System.out.print("Nhập ngày sản xuất (dd/MM/yyyy): ");
            String dateStartStr = scanner.nextLine();
            try {
                dateStart = LocalDate.parse(dateStartStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng ngày không hợp lệ. Vui lòng thử lại.");
            }
        }

        LocalDate dateEnd = null;
        while (dateEnd == null) {
            System.out.print("Nhập ngày hết hạn (dd/MM/yyyy): ");
            String dateEndStr = scanner.nextLine();
            try {
                dateEnd = LocalDate.parse(dateEndStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                // Kiểm tra ngày hết hạn phải sau ngày sản xuất
                if (dateEnd.isBefore(dateStart)) {
                    System.out.println("Ngày hết hạn phải sau ngày sản xuất. Vui lòng thử lại.");
                    dateEnd = null;
                }
            } catch (DateTimeParseException e) {
                System.out.println("Định dạng ngày không hợp lệ. Vui lòng thử lại.");
            }
        }

        // Tạo giao dịch mới
        Transaction transaction = new Transaction(unitName, codeProduct, nameProduct, dateStart, dateEnd,
                System.currentTimeMillis());
        pendingTransactions.add(transaction);

        System.out.println("Đã thêm giao dịch vào danh sách chờ.");
        System.out.println("Hash giao dịch: " + transaction.caCalculateTransactionHash());
        System.out.println("Chữ ký số: " + transaction.signTransaction());
    }

    private static void createNewBlock(BlockChain blockchain) {
        if (pendingTransactions.isEmpty()) {
            System.out.println("Không có giao dịch đang chờ. Hãy thêm giao dịch trước.");
            return;
        }

        int nextBlockNumber = blockchain.getNextBlockNumber();
        Block newBlock = new Block(nextBlockNumber);

        // Thêm tối đa 4 giao dịch vào block
        int count = 0;
        while (!pendingTransactions.isEmpty() && count < 4) {
            Transaction transaction = pendingTransactions.remove(0);
            newBlock.addTransaction(transaction);
            count++;
        }

        // Thiết lập hash cho block
        newBlock.setBlockHash(blockchain.getCurrentBlock());

        // Thêm block vào blockchain
        blockchain.acceptBlock(newBlock);

        System.out.println("Đã tạo Block #" + nextBlockNumber + " với " + count + " giao dịch.");
        System.out.println("Block hash: " + newBlock.getBlockHash());
    }

    private static void verifyTransaction() {
        if (pendingTransactions.isEmpty()) {
            System.out.println("Không có giao dịch đang chờ để xác minh.");
            return;
        }

        System.out.println("\n=== Xác Minh Giao Dịch ===");
        System.out.println("Danh sách giao dịch đang chờ:");

        for (int i = 0; i < pendingTransactions.size(); i++) {
            Transaction transaction = pendingTransactions.get(i);
            System.out.println((i + 1) + ". " + transaction.getNameProduct() + " - " + transaction.getCodeProduct());
        }

        System.out.println("Đang xác minh tất cả các giao dịch...");

        for (int i = 0; i < pendingTransactions.size(); i++) {
            Transaction transaction = pendingTransactions.get(i);
            boolean isValid = transaction.isValidSignature();

            System.out.println("Giao dịch #" + (i + 1) + ": " + (isValid ? "HỢP LỆ" : "KHÔNG HỢP LỆ"));
        }
    }

        private static void verifyBlockchain(BlockChain blockchain) {
            if (blockchain.getHeadBlock() == null) {
                System.out.println("Blockchain trống. Không có gì để xác minh.");
                return;
            }
        
            System.out.println("\n=== Xác Minh Tính Toàn Vẹn Của Blockchain ===");
        
            try {
                // Chuẩn bị các cấu trúc dữ liệu để lưu trữ thông tin về block và giao dịch không hợp lệ
                List<IBlock> invalidBlocks = new ArrayList<>();
                Map<Integer, List<Map<String, String>>> invalidTransactions = new HashMap<>();
                
                // Gọi phương thức verifyChainDetailed đã thêm vào lớp BlockChain
                boolean isValid = blockchain.verifyChainDetailed(null, true, invalidBlocks, invalidTransactions);
                
                if (isValid) {
                    System.out.println("Blockchain integrity intact - Chuỗi khối hoàn toàn nguyên vẹn.");
                } else {
                    System.out.println("Blockchain integrity NOT intact - Chuỗi khối đã bị sửa đổi!");
                    
                    // Hiển thị thông tin chi tiết về các block và giao dịch bị sửa đổi
                    System.out.println("\nChi tiết về các block và giao dịch bị sửa đổi:");
                    
                    for (IBlock iblock : invalidBlocks) {
                        Block block = (Block) iblock;
                        System.out.println("\n--- Block #" + block.getBlockNumber() + " bị sửa đổi ---");
                        System.out.println("Ngày tạo: " + block.getCreatedDate());
                        System.out.println("Hash hiện tại: " + block.getBlockHash());
                        System.out.println("Hash tính toán lại: " + block.calculateBlockHash(block.getPreviousBlockHash()));
                        
                        // Hiển thị các giao dịch bị sửa đổi trong block
                        List<Map<String, String>> txnInfoList = invalidTransactions.get(block.getBlockNumber());
                        if (txnInfoList != null && !txnInfoList.isEmpty()) {
                            System.out.println("\nCác giao dịch bị sửa đổi trong block này:");
                            for (Map<String, String> txnInfo : txnInfoList) {
                                System.out.println("  Đơn vị sản xuất: Từ " + txnInfo.get("unitNameFile") + 
                                " thành " + txnInfo.get("unitNameMemory"));
                                System.out.println("  Mã sản phẩm: Từ " + txnInfo.get("codeProductFile") + 
                                " thành " + txnInfo.get("codeProductMemory"));
                                System.out.println("  Tên sản phẩm: Từ " + txnInfo.get("nameProductFile") + 
                                " thành " + txnInfo.get("nameProductMemory"));
                                System.out.println("  Ngày sản xuất: Từ " + txnInfo.get("dateStartFile") + 
                                " thành " + txnInfo.get("dateStartMemory"));
                                System.out.println("  Ngày hết hạn: Từ " + txnInfo.get("dateEndFile") + 
                                " thành " + txnInfo.get("dateEndMemory"));
                                System.out.println();
                            }
                        } else {
                            System.out.println("Không có giao dịch cụ thể bị sửa đổi, nhưng cấu trúc block đã thay đổi.");
                        }
                    }
                }
            } catch (IllegalStateException e) {
                System.out.println("Lỗi: " + e.getMessage());
            }
        }

    private static void saveBlockchain(BlockChain blockchain, Scanner scanner) {
        if (blockchain.getHeadBlock() == null) {
            System.out.println("Blockchain trống. Không có gì để lưu.");
            return;
        }

        System.out.print("Nhập đường dẫn file để lưu (ví dụ: blockchain.txt): ");
        String filePath = scanner.nextLine();

        Storage.saveToFile(blockchain, filePath);
    }

    private static BlockChain loadBlockchain(Scanner scanner) {
        System.out.print("Nhập đường dẫn file để đọc: ");
        String filePath = scanner.nextLine();
    
        BlockChain loadedBlockchain = Storage.readFromFile(filePath);
        if (loadedBlockchain != null) {
            System.out.println("Đã đọc blockchain từ file thành công.");
            return loadedBlockchain;
        } else {
            System.out.println("Không thể đọc blockchain từ file.");
            return null;
        }
    }

    private static void displayBlockchainInfo(BlockChain blockchain) {
        if (blockchain.getHeadBlock() == null) {
            System.out.println("Blockchain trống.");
            return;
        }

        System.out.println("\n=== Thông Tin Blockchain ===");
        System.out.println("Số lượng block: " + blockchain.getBlocks().size());

        for (int i = 0; i < blockchain.getBlocks().size(); i++) {
            Block block = (Block) blockchain.getBlocks().get(i);
            System.out.println("\nBlock #" + block.getBlockNumber());
            System.out.println("Ngày tạo: " + block.getCreatedDate());
            System.out.println("Hash: " + block.getBlockHash());
            System.out.println("Previous Hash: "
                    + (block.getPreviousBlockHash() != null ? block.getPreviousBlockHash() : "null (Genesis Block)"));
            System.out.println("Merkle Root: " + block.getMerkleRootHash());

            List<ITransaction> transactions = block.getTransaction();
            System.out.println("Số lượng giao dịch: " + transactions.size());

            for (int j = 0; j < transactions.size(); j++) {
                Transaction transaction = (Transaction) transactions.get(j);
                System.out.println("\n  Giao dịch #" + (j + 1));
                System.out.println("  Đơn vị sản xuất: " + transaction.getUnitName());
                System.out.println("  Mã sản phẩm: " + transaction.getCodeProduct());
                System.out.println("  Tên sản phẩm: " + transaction.getNameProduct());
                System.out.println("  Ngày sản xuất: " + transaction.getDateStart());
                System.out.println("  Ngày hết hạn: " + transaction.getDateEnd());
                System.out.println("  Hash: " + transaction.caCalculateTransactionHash());
                System.out.println("  Chữ ký: " + transaction.signTransaction());
            }
        }
    }
}