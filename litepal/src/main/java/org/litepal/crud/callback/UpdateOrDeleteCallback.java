package org.litepal.crud.callback;

import android.database.Cursor;

/**
 * @author guolin
 * @since 2017/2/22
 */
public interface UpdateOrDeleteCallback {

    void onFinish(int rowsAffected);

}