package com.fastaoe.ioc;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by jinjin on 17/5/13.
 */

public interface ViewInject {

    /**
     * 注入View
     *
     * @param view
     */
    void inject(View view);

    /**
     * 注入ViewHolder
     *
     * @param view
     * @param handler
     */
    void inject(Object handler, View view);

    /**
     * 注入Activity
     *
     * @param activity
     */
    void inject(Activity activity);

    /**
     * 注入Fragment
     *
     * @param fragment
     */
    View inject(Object fragment, LayoutInflater inflater, ViewGroup container);
}
