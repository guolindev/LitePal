package org.litepal.crud.async;

import org.litepal.crud.callback.FindBySQLCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class FindBySQLExecutor extends AsyncExecutor {

    private FindBySQLCallback cb;

    public void listen(FindBySQLCallback callback) {
        cb = callback;
        execute();
    }

    public FindBySQLCallback getListener() {
        return  cb;
    }

}