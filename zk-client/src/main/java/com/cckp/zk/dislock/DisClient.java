package com.cckp.zk.dislock;

import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;

/**
 * @author ljs
 * @version 1.0
 * @description
 * @date 2021/6/28 9:38
 */
public class DisClient {

    private final static String LOCK_PATH = "/distributedLock";

    private ZkClient zkClient = new ZkClient("aliyun-server:2181,aliyun-server:2182,aliyun-server:2183");

    /**
     * 创建临时顺序节点并将序号返回
     */
    private String createTempSNode() {
        String lock = zkClient.createEphemeralSequential(LOCK_PATH + "/", "lock");
        return lock.replace(LOCK_PATH + "/", "");
    }

    private String getMinNode() {
        List<String> children = zkClient.getChildren(LOCK_PATH);
        Collections.sort(children);
        return children.get(0);
    }


    public void getDisLock() {
        /**
         * step1:创建临时顺序节点
         * step2:获取当前最小序号
         * step3: 尝试获取锁
         *  - 获取成功
         *  - 获取失败
         *      等待通知->接收到通知->再次尝试获取锁
         * */
        String tempSNode = createTempSNode();
        String minNode = getMinNode();
        tryGetLock(tempSNode, minNode);

    }

    public void tryGetLock(String curNode, String minNode) {
        String name = Thread.currentThread().getName();
        if (!curNode.equals(minNode)) {
            System.out.println("线程" + name + "没有获取到锁");
            waitForLock();
            tryGetLock(curNode, getMinNode());
        } else {
            System.out.println("线程" + name + "获取到锁了");
        }
    }

    //等待之前节点锁释放，如何判断锁被释放，需要唤醒线程继续尝试tryGetLock()
    public void waitForLock() {

    }


}
