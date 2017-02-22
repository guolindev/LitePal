package org.litepal.crud.callback;

import android.database.Cursor;

/**
 * @author guolin
 * @since 2017/2/22
 */
public interface FindBySQLCallback {

    void onFinish(Cursor cursor);

}