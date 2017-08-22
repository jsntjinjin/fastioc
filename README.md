# fastioc

简单好用的Android 运行时View注解框架

### 引入

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2. Add the dependency

```
dependencies {
        compile 'com.github.jsntjinjin:fastioc:9b027f0c44'
}
```

### 如何使用

Activity、Fragment中

```
// activity
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ViewInjectorImpl.getInstance().inject(this);
}

// fragment
public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return ViewInjectorImpl.getInstance().inject(this, inflater, container);
}
```

添加布局
```
@ContentView(R.layout.activity_main)
public class MainActivity extends BaseActivity {
}
```

绑定View
```
@Bind(R.id.viewpager)
ViewPager viewpager;
```

绑定事件并检查网络
```
@OnClick(R.id.banner)
@CheckNet
void showBanner(TextView tv) {
    Intent intent = new Intent(getActivity(), BannerViewActivity.class);
    startActivity(intent);
}
```