package com.cckp.zk.dislock;

/**
 * @author ljs
 * @version 1.0
 * @description
 * @date 2021/6/28 9:03
 */
public class DisLockTest {
    //多个线程模拟分布式环境
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(new DisLockRunnable()).start();
        }
    }
    static class DisLockRunnable implements Runnable{

        @Override
        public void run() {
            //抢锁
            DisClient client = new DisClient();
            client.getDisLock();
        }
    }
}
