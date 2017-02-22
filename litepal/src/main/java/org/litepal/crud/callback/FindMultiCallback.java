package org.litepal.crud.callback;

import java.util.List;

/**
 * @author guolin
 * @since 2017/2/22
 */
public interface FindMultiCallback {

    <T> void onFinish(List<T> t);

}