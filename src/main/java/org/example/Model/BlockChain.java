package org.example.Model;

import org.example.Interface.IBlock;
import org.example.Interface.IBlockChain;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockChain implements IBlockChain {
    private IBlock currentBlock;
    private IBlock headBlock;
    private List<IBlock> blocks;

    public IBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(IBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    public IBlock getHeadBlock() {
        return headBlock;
    }

    public void setHeadBlock(IBlock headBlock) {
        this.headBlock = headBlock;
    }

    public List<IBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<IBlock> blocks) {
        this.blocks = blocks;
    }

    @Override
    public void acceptBlock(IBlock block) {
        if (headBlock == null) {
            headBlock = block;
            headBlock.setPreviousBlockHash(null);
        }
        currentBlock = block;
        blocks.add(block);
    }

    @Override
    public int getNextBlockNumber() {
        if (headBlock == null) {
            return 0;
        }
        return currentBlock.getBlockNumber() + 1;
    }

    @Override
    public void verifyChain() {
        if (headBlock == null) {
            throw new IllegalStateException("Genesis block not set.");
        }

        List<IBlock> invalidBlocks = new ArrayList<>();
        Map<Integer, List<Map<String, String>>> invalidTransactions = new HashMap<>();

        boolean isValid = verifyChainDetailed(null, true, invalidBlocks, invalidTransactions);

        if (isValid) {
            System.out.println("Blockchain integrity intact.");
        } else {
            System.out.println("Blockchain integrity NOT intact.");

            // Hiển thị thông tin chi tiết về các block và giao dịch bị sửa đổi
            System.out.println("\nChi tiết về các block và giao dịch bị sửa đổi:");

            for (IBlock block : invalidBlocks) {
                System.out.println("\nBlock #" + block.getBlockNumber() + " bị sửa đổi");
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
                }
            }
        }
    }

    // Phương thức mới để kiểm tra chi tiết và thu thập thông tin về các block và giao dịch không hợp lệ
    public boolean verifyChainDetailed(String prevBlockHash, boolean verbose,
                                        List<IBlock> invalidBlocks,
                                        Map<Integer, List<Map<String, String>>> invalidTransactions) {
        return ((Block)headBlock).isValidChainDetailed(prevBlockHash, verbose, invalidBlocks, invalidTransactions);
    }

}
