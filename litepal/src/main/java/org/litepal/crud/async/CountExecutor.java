package org.litepal.crud.async;

import org.litepal.crud.callback.CountCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class CountExecutor extends AsyncExecutor {

    private CountCallback cb;

    public void listen(CountCallback callback) {
        cb = callback;
        execute();
    }

    public CountCallback getListener() {
        return  cb;
    }

}