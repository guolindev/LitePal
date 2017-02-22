package org.litepal.crud.async;

import org.litepal.crud.callback.FindCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class FindExecutor extends AsyncExecutor {

    private FindCallback cb;

    public void listen(FindCallback callback) {
        cb = callback;
        execute();
    }

    public FindCallback getListener() {
        return  cb;
    }

}
