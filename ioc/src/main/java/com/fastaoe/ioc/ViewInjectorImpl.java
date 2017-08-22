package com.fastaoe.ioc;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by jinjin on 17/5/13.
 */

public class ViewInjectorImpl implements ViewInject {

    private ViewInjectorImpl() {
    }

    public static ViewInjectorImpl mInstance;

    public static ViewInjectorImpl getInstance() {
        if (mInstance == null) {
            synchronized (ViewInjectorImpl.class) {
                if (mInstance == null) {
                    mInstance = new ViewInjectorImpl();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void inject(View view) {
        injectObject(view, view.getClass(), new ViewFinder(view));
    }

    @Override
    public void inject(Object handler, View view) {
        injectObject(handler, view.getClass(), new ViewFinder(view));
    }

    @Override
    public void inject(Activity activity) {
        Class<?> clazz = activity.getClass();
        // activity设置布局
        try {
            ContentView contentView = findContentView(clazz);
            if (contentView != null) {
                int layoutId = contentView.value();
                if (layoutId > 0) {
                    Method setContentView = clazz.getMethod("setContentView", int.class);
                    setContentView.invoke(activity, layoutId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        injectObject(activity, clazz, new ViewFinder(activity));
    }

    @Override
    public View inject(Object fragment, LayoutInflater inflater, ViewGroup container) {
        Class<?> clazz = fragment.getClass();
        // fragment设置布局
        View view = null;
        ContentView contentView = findContentView(clazz);
        if (contentView != null) {
            int layoutId = contentView.value();
            if (layoutId > 0) {
                view = inflater.inflate(layoutId, container, false);
            }
        }
        injectObject(fragment, clazz, new ViewFinder(view));
        return view;
    }

    /**
     * 从类中获取ContentView注解
     *
     * @param clazz
     * @return
     */
    private static ContentView findContentView(Class<?> clazz) {
        return clazz != null ? clazz.getAnnotation(ContentView.class) : null;
    }

    public static void injectObject(Object handler, Class<?> clazz, ViewFinder finder) {
        try {
            injectView(handler, clazz, finder);
            injectEvent(handler, clazz, finder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置findViewById
     *
     * @param handler
     * @param clazz
     * @param finder
     */
    @SuppressWarnings("ConstantConditions")
    private static void injectView(Object handler, Class<?> clazz, ViewFinder finder) throws Exception {
        // 获取class的所有属性
        Field[] fields = clazz.getDeclaredFields();

        // 遍历并找到所有的Bind注解的属性
        for (Field field : fields) {
            Bind viewById = field.getAnnotation(Bind.class);
            if (viewById != null) {
                // 获取View
                View view = finder.findViewById(viewById.value(), viewById.parentId());
                if (view != null) {
                    // 反射注入view
                    field.setAccessible(true);
                    field.set(handler, view);
                } else {
                    throw new Exception("Invalid @Bind for "
                            + clazz.getSimpleName() + "." + field.getName());
                }
            }

        }
    }

    /**
     * 设置Event
     *
     * @param handler
     * @param clazz
     * @param finder
     */
    @SuppressWarnings("ConstantConditions")
    private static void injectEvent(Object handler, Class<?> clazz, ViewFinder finder) throws Exception {
        // 获取class所有的方法
        Method[] methods = clazz.getDeclaredMethods();

        // 遍历找到onClick注解的方法
        for (Method method : methods) {
            OnClick onClick = method.getAnnotation(OnClick.class);
            OnItemClick onItemClick = method.getAnnotation(OnItemClick.class);
            boolean checkNet = method.getAnnotation(CheckNet.class) != null;
            if (onClick != null) {
                // 获取注解中的value值
                int[] views = onClick.value();
                int[] parentIds = onClick.parentId();
                int parentLen = parentIds == null ? 0 : parentIds.length;
                for (int i = 0; i < views.length; i++) {
                    // findViewById找到View
                    int viewId = views[i];
                    int parentId = parentLen > i ? parentIds[i] : 0;
                    View view = finder.findViewById(viewId, parentId);
                    if (view != null) {
                        // 设置setOnClickListener反射注入方法
                        view.setOnClickListener(new MyOnClickListener(method, handler, checkNet));
                    } else {
                        throw new Exception("Invalid @OnClick for "
                                + clazz.getSimpleName() + "." + method.getName());
                    }
                }
            }

            if (onItemClick != null) {
                // 获取注解中的value值
                int[] views = onItemClick.value();
                int[] parentIds = onItemClick.parentId();
                int parentLen = parentIds == null ? 0 : parentIds.length;
                for (int i = 0; i < views.length; i++) {
                    // findViewById找到View
                    int viewId = views[i];
                    int parentId = parentLen > i ? parentIds[i] : 0;
                    AdapterView view = (AdapterView) finder.findViewById(viewId, parentId);
                    if (view != null) {
                        // 设置setOnItemClickListener反射注入方法
                        view.setOnItemClickListener(new MyOnItemClickListener(method, handler, checkNet));
                    } else {
                        throw new Exception("Invalid @OnItemClick for "
                                + clazz.getSimpleName() + "." + method.getName());
                    }
                }
            }
        }
    }

    private static class MyOnClickListener implements View.OnClickListener {
        private Method method;
        private Object handler;
        private boolean checkNet;

        public MyOnClickListener(Method method, Object handler, boolean checkNet) {
            this.method = method;
            this.handler = handler;
            this.checkNet = checkNet;
        }

        @Override
        public void onClick(View v) {
            if (checkNet && !NetStateUtil.isNetworkConnected(v.getContext())) {
                Toast.makeText(v.getContext(), "网络错误!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 注入方法
            try {
                method.setAccessible(true);
                method.invoke(handler, v);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        private Method method;
        private Object handler;
        private boolean checkNet;

        public MyOnItemClickListener(Method method, Object handler, boolean checkNet) {
            this.method = method;
            this.handler = handler;
            this.checkNet = checkNet;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            if (checkNet && !NetStateUtil.isNetworkConnected(v.getContext())) {
                Toast.makeText(v.getContext(), "网络错误!", Toast.LENGTH_SHORT).show();
                return;
            }

            // 注入方法
            try {
                method.setAccessible(true);
                method.invoke(handler, parent, v, position, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
