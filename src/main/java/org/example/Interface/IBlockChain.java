package org.example.Interface;

public interface IBlockChain {
    void acceptBlock(IBlock block);
    int getNextBlockNumber();
    void verifyChain();
}
