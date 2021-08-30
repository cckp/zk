package com.cckp.jvm.forkjoin;

import java.util.concurrent.RecursiveAction;

/**
 * @author ljs
 * @version 1.0
 * @description
 * @date 2021/8/30 17:19
 */
public class SortTask extends RecursiveAction {

    final long[] array;
    final long lo;
    final long hi;

    public SortTask(long[] array) {
        this.array = array;
        this.lo = 0;
        this.hi = array.length - 1;
    }

    public SortTask(long[] array, long lo, long hi) {
        this.array = array;
        this.lo = lo;
        this.hi = hi;
    }

    @Override
    protected void compute() {
        if (lo < hi) {
            //划分
        }
    }

    private long partition(long[] array,long lo,long hi){
        return -1L;
    }
}
