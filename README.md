[![](https://jitpack.io/v/Yumenokanata/FragmentManager.svg)](https://jitpack.io/#Yumenokanata/FragmentManager)

# FragmentManager
Fragment切换方法的封装  
1. 提供多栈管理（官方的fragment是单栈的）  
2. 简化Fragment的管理  
3. 完全模仿Activity的接口方法和回调（如startFragment()、finish()等）  
4. 通过Intent启动，通过onFragmentResult()方法回调  
5. 使用Rx的方式启动Fragment: startFragmentForObservable(Intent intent)  
6. 自定义Fragment切换动画  
7. (__2.4.0 NEW__)加入侧滑返回

## 添加到Android studio
Step1: 在根build.gradle中添加仓库：
```groovy
allprojects {
	repositories {
        jcenter()
		maven { url "https://jitpack.io" }
	}
}
```

Step2: 在工程中添加依赖：
```groovy
dependencies {
    compile 'com.github.Yumenokanata:FragmentManager:x.y.z'
}
```

## 

## 使用方法
1、 使用的Activity和Fragment分别应继承自BaseFragmentManagerActivity和BaseManagerFragment
```java
public class MyActivity extends BaseFragmentManagerActivity
```
```java
public class MyFragment extends BaseManagerFragment
```

2、在Activity中需要实现两个方法：
```java
public class MyActivity extends BaseFragmentManagerActivity {
    //此方法中需返回fragment显示在的View的id
    public int fragmentViewId() { ... }
    //此方法中需要返回多栈的栈名以及所对应的默认Fragment
    public Map<String, Class<?>> BaseFragmentWithTag() { ... }
}
```
注：之后可以通过switchToStackByTag(String tag)方法在不同的栈直接进行切换

3、Fragment中不必要实现任何方法

4、Fragment中的使用
```java
startFragment(Intent)  //通过Intent启动一个Fragment
onFragmentResult(Intent) //通过Intent启动一个Fragment，并在这个新Fragment结束后回调onFragmentResult方法
finish() //结束当前的Fragment
startFragmentOnNewActivity(Intent intent, Class<? extends SingleBaseActivity> activityClazz) //在新Activity中启动Fragment

startFragmentForObservable(Intent intent) // 使用Rxjava的方式启动新的Fragment
startActivityForObservable(Intent intent) // 使用Rxjava的方式启动新的Activity
//Rx方式返回的是Observable<Tuple<Integer, Bundle>>, 分别对应resultCode和bundle(此种启动方式时，requestCode会由框架自己生成和管理，所以不能直接传入和获取到)

onFragmentResult(int requestCode, int resultCode, Bundle data) //回调方法
```

5、Activity中的使用
```java
switchToStackByTag(String tag)  //切换栈
```

6、自定义Fragment进入和退出动画
```java
BaseFragmentManagerActivity.setFragmentAnim(int enterAnim, int exitAnim)
```

7、(**2.4.0修改了onShow()回调方法**) 新增两个生命周期方法：onShow()和onHide()  
onHide方法只会在Fragment由显示状态切换为隐藏状态时调用，这意味着Fragment被销毁时不会回调此方法。  
对于startFragmentOnNewActivity()这两个生命周期方法同样有效。
> (2.4.0以下版本)onShow方法只会在Fragment由隐藏状态转换为显示状态时被调用，并且是在界面切换动画完成后调用（这意味着onShow可能在onCreateView方法调用完成后一段时间后才会调用）;  

(2.4.0及以上版本)onShow()方法有了一个参数：@OnShowMode int callMode, 有四种可能的值：  
1. OnShowMode.**ON_CREATE**: 在Fragment创建时回调并传入  
2. OnShowMode.**ON_RESUME**: 实现是在原生的onResumeFragments方法中回调，有两种情况会被回调：1) 通过各种startActivity方法启动一个Activity后返回的 2) 应用从后台返回时  
3. OnShowMode.**ON_BACK**: 在从启动的下一个Fragment返回时回调(注意，从下一个Activity中返回时并不会回调onBack)  
4. OnShowMode.**ON_SWITCH**: 在通过switchToStackByTag()方法切换栈时，对切换到的栈的可见Fragment(即最上层的)回调  

8、(**2.4.0 NEW**)新增侧滑返回
通过SwipeBackUtil中的静态方法对Activity或者Fragment添加侧滑返回的功能

对Fragment:  
在BaseManagerFragment的onCreateView()方法中添加
```java
return SwipeBackUtil.enableSwipeBackAtFragment(this, view);
```
eg:
```java
@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_blank_fragment12, container, false);
    return SwipeBackUtil.enableSwipeBackAtFragment(this, view);
}
```

对Activity:  
在BaseFragmentManagerActivity的onPostCreate()方法中添加:
```java
SwipeBackUtil.enableSwipeBackAtActivity(this);
```
建议用于SingleBaseActivity

eg:
```java
@Override
protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    SwipeBackUtil.enableSwipeBackAtActivity(this);
}
```


###License
<pre>
Copyright 2015 Yumenokanata

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>
