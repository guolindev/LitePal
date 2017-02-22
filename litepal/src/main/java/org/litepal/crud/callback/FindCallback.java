package org.litepal.crud.callback;

/**
 * @author guolin
 * @since 2017/2/22
 */
public interface FindCallback {

    <T> void onFinish(T t);

}
