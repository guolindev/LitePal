package org.litepal.crud.async;

import org.litepal.crud.callback.AverageCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class AverageExecutor extends AsyncExecutor {

    private AverageCallback cb;

    public void listen(AverageCallback callback) {
        cb = callback;
        execute();
    }

    public AverageCallback getListener() {
        return  cb;
    }

}