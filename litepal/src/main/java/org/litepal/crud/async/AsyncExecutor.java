package org.litepal.crud.async;

/**
 * @author guolin
 * @since 2017/2/22
 */
public abstract class AsyncExecutor {

    private Runnable pendingTask;

    public void submit(Runnable task) {
        pendingTask = task;
    }

    void execute() {
        if (pendingTask != null) {
            new Thread(pendingTask).start();
        }
    }

}