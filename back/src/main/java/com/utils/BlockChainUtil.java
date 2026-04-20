package com.utils;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;
import java.math.BigInteger;

public class BlockChainUtil {
    // 平台公证人的超级私钥（系统帮老师付 Gas 费，老师完全无感知）
    private static final String ADMIN_PRIVATE_KEY = "0x4f3edf983ac636a65a842ce7c78d9aa706d3b113bce9c46f30d7d21715b23b1d";

    // 1. 为老师生成身份地址
    public static String generateWalletAddress() throws Exception {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        return Credentials.create(ecKeyPair).getAddress();
    }

    // 2. 将数据摘要打包上链
    public static String submitResearchToChain(String dataId, String title, String ipfsHash, String ownerAddress) {
        try {
            Web3j web3j = Web3j.build(new HttpService("http://127.0.0.1:8545"));
            Credentials admin = Credentials.create(ADMIN_PRIVATE_KEY);

            // 拼装上链信息（加入UTF-8防止中文乱码）
            String rawData = String.format("ResearchID:%s|Title:%s|IPFS:%s|Owner:%s", dataId, title, ipfsHash, ownerAddress);
            String hexData = Numeric.toHexString(rawData.getBytes("UTF-8"));

            FastRawTransactionManager manager = new FastRawTransactionManager(web3j, admin);
            EthSendTransaction tx = manager.sendTransaction(
                    DefaultGasProvider.GAS_PRICE,
                    BigInteger.valueOf(3000000),
                    admin.getAddress(), // 把哈希打包给自己
                    hexData,
                    BigInteger.ZERO
            );
            return tx.getTransactionHash(); // 返回 0x 开头的交易哈希
        } catch (Exception e) {
            System.err.println("❌ 区块链上链失败：" + e.getMessage());
            return "CHAIN_FAILED";
        }
    }
}