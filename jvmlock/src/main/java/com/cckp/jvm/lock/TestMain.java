package com.cckp.jvm.lock;

import org.openjdk.jol.info.ClassLayout;

/**
 * @author ljs
 * @version 1.0
 * @description
 * @date 2021/8/25 15:47
 */
public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        /*
         * https://blog.csdn.net/do_finsh/article/details/105364513
         *
         * 关闭指针压缩：-XX:-UseCompressedOops
         * */
        printfCurThreadInfo();
        Thread.sleep(10000);
        LockInstance lockInstance = new LockInstance();
        System.out.println("new 对象之后");
        System.out.println(ClassLayout.parseInstance(lockInstance).toPrintable());
        synchronized (lockInstance) {
            Thread.sleep(1000);
            System.out.println("获取锁时");
            printfCurThreadInfo();
            System.out.println(ClassLayout.parseInstance(lockInstance).toPrintable());
            Thread.sleep(1000);
            synchronized (lockInstance) {
                Thread.sleep(1000);
                System.out.println("重入时");
                printfCurThreadInfo();
                System.out.println(ClassLayout.parseInstance(lockInstance).toPrintable());
                Thread.sleep(1000);
            }
        }
        System.out.println("释放锁之后");
        printfCurThreadInfo();
        System.out.println(ClassLayout.parseInstance(lockInstance).toPrintable());

    }

    private static void printfCurThreadInfo() {
        long id = Thread.currentThread().getId();
        System.out.println("当前线程ID（十进制）:" + id);
        System.out.println("当前线程ID（二进制）:" + Long.toBinaryString(id));
    }
}
