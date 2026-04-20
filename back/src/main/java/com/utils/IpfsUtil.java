package com.utils;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import java.io.File;

public class IpfsUtil {
    // 连接本地 Docker 跑起来的 IPFS 节点
    private static IPFS ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");

    // 接收本地生成的 File 对象，上传到 IPFS
    public static String upload(File file) {
        try {
            NamedStreamable.FileWrapper is = new NamedStreamable.FileWrapper(file);
            MerkleNode addResult = ipfs.add(is).get(0);
            return addResult.hash.toBase58(); // 返回 Qm 开头的绝对唯一防篡改CID
        } catch (Exception e) {
            System.err.println("❌ IPFS上传失败：" + e.getMessage());
            return null;
        }
    }
}
