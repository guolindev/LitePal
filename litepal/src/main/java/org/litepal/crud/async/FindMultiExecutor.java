package org.litepal.crud.async;

import org.litepal.crud.callback.FindMultiCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class FindMultiExecutor extends AsyncExecutor {

    private FindMultiCallback cb;

    public void listen(FindMultiCallback callback) {
        cb = callback;
        execute();
    }

    public FindMultiCallback getListener() {
        return  cb;
    }

}
