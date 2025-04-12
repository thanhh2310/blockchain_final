package org.example.Interface;

import java.time.LocalDate;
import java.util.List;

public interface IBlock {
    // List of transactions
    List<ITransaction> getTransaction();

    // Block header data
    int getBlockNumber();
    LocalDate getCreatedDate();
    void setCreatedDate(LocalDate date);
    String getBlockHash();
    String getPreviousBlockHash();
    void setPreviousBlockHash(String hash);

    void addTransaction(ITransaction transaction);
    String calculateBlockHash(String previousBlockHash);
    void setBlockHash(IBlock parent);
    IBlock getNextBlock();
    void setNextBlock(IBlock nextBlock);
    boolean isValidChain(String prevBlockHash, boolean verbose);

    String getMerkleRootHash();
}
