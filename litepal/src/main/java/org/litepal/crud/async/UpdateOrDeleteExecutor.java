package org.litepal.crud.async;

import org.litepal.crud.callback.UpdateOrDeleteCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class UpdateOrDeleteExecutor extends AsyncExecutor {

    private UpdateOrDeleteCallback cb;

    public void listen(UpdateOrDeleteCallback callback) {
        cb = callback;
        execute();
    }

    public UpdateOrDeleteCallback getListener() {
        return  cb;
    }

}