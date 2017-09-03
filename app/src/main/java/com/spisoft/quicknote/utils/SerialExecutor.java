package com.spisoft.quicknote.utils;

import android.os.AsyncTask;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;

/**
 * Created by alexandre on 05/02/16.
 */
public class SerialExecutor implements Executor {
    final ArrayDeque<Runnable> mTasks = new ArrayDeque<Runnable>();
    Runnable mActive;

    public SerialExecutor(){

    }
    public synchronized void execute(final Runnable r) {
        mTasks.offer(new Runnable() {
            public void run() {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            }
        });
        if (mActive == null) {
            scheduleNext();
        }
    }

    protected synchronized void scheduleNext() {
        if ((mActive = mTasks.poll()) != null) {

            AsyncTask.THREAD_POOL_EXECUTOR.execute(mActive);
        }
    }
}
