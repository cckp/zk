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

    private String getBeforeNode(List<String> children, String node) {
        int index = Collections.binarySearch(children, node);
        return children.get(index - 1);
    }


    public void getDisLock() {
        getDisLock(null, null);
    }

    private void getDisLock(String curNode, List<String> children) {
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
            //获取curNode 极其前面的 节点 的集合
            children = zkClient.getChildren(LOCK_PATH);
            Collections.sort(children);
        }
        String name = Thread.currentThread().getName();
        if (tryGetLock(curNode, children.get(0))) {
            //获取锁成功
            System.out.println("获取锁成功:" + name+" curName:"+curNode);
        } else {
            System.out.println("获取锁失败:" + name+" curName:"+curNode);
            //获取锁失败
            waitForLock(curNode, children);
            getDisLock(curNode, children);
        }

    }

    private boolean tryGetLock(String curNode, String minNode) {
        return Objects.equals(curNode, minNode);
    }

    //等待之前节点锁释放，如何判断锁被释放，需要唤醒线程继续尝试tryGetLock()
    private void waitForLock(String curNode, List<String> children) {
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
        String beforeNode = getBeforeNode(children, curNode);
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
