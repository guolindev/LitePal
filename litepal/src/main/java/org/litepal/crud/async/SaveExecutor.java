package org.litepal.crud.async;

import org.litepal.crud.callback.SaveCallback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public class SaveExecutor extends AsyncExecutor {

    private SaveCallback cb;

    public void listen(SaveCallback callback) {
        cb = callback;
        execute();
    }

    public SaveCallback getListener() {
        return  cb;
    }

}