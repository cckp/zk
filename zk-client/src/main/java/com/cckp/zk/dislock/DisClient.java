package com.cckp.zk.dislock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * @author ljs
 * @version 1.0
 * @description
 * @date 2021/6/28 9:38
 */
public class DisClient {

    private final static String LOCK_PATH = "/distributedLock";

    private ZkClient zkClient = new ZkClient("aliyun-server:2181,aliyun-server:2182,aliyun-server:2183");

    private CountDownLatch countDownLatch;

    public DisClient() {
        if (!zkClient.exists(LOCK_PATH)) {
            synchronized (DisClient.class) {
                if (!zkClient.exists(LOCK_PATH)) {
                    zkClient.createPersistent(LOCK_PATH);
                }
            }
        }
    }

    /**
     * 创建临时顺序节点并将序号返回
     */
    private String createTempSNode() {
        String lock = zkClient.createEphemeralSequential(LOCK_PATH + "/", "lock");
        return lock.replace(LOCK_PATH + "/", "");
    }


    public void getDisLock() {
        getDisLock(null);
    }

    private void getDisLock(String curNode) {
        /**
         * step1:创建临时顺序节点
         * step2:获取当前最小序号
         * step3: 尝试获取锁
         *  - 获取成功
         *  - 获取失败
         *      等待通知->接收到通知->再次尝试获取锁
         * */
        if (curNode == null) {
            curNode = createTempSNode();
        }
        String name = Thread.currentThread().getName();
        if (tryGetLock(curNode)) {
            //获取锁成功
            System.out.println("获取锁成功:" + name + " curName:" + curNode);
            //删除节点
            zkClient.delete(LOCK_PATH+"/"+curNode);
        } else {
            System.out.println("获取锁失败:" + name + " curName:" + curNode);
            //获取锁失败
            waitForLock(curNode);
            getDisLock(curNode);
        }

    }

    private boolean tryGetLock(String curNode) {
        List<String> children = zkClient.getChildren(LOCK_PATH);
        Collections.sort(children);
        String minNode = children.get(0);
        return Objects.equals(curNode, minNode);
    }

    //等待之前节点锁释放，如何判断锁被释放，需要唤醒线程继续尝试tryGetLock()
    private void waitForLock(String curNode) {

        IZkDataListener iZkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String s, Object o) throws Exception {

            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                //删除是通知线程再次获取锁
                countDownLatch.countDown();
            }
        };
        //获取curNode 及其前面的 节点 的集合
        List<String> children = zkClient.getChildren(LOCK_PATH);
        Collections.sort(children);
        int index = Collections.binarySearch(children, curNode);
        String beforeNode = children.get(index - 1);

        zkClient.subscribeDataChanges(LOCK_PATH + "/" + beforeNode, iZkDataListener);

        if (zkClient.exists(LOCK_PATH + "/" + beforeNode)) {
            //等待beforeNode的删除通知
            countDownLatch = new CountDownLatch(1);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            //解除监听+再次尝试获取锁
            zkClient.unsubscribeDataChanges(LOCK_PATH + "/" + beforeNode, iZkDataListener);
        }
    }


}
