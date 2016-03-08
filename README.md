# FragmentManager
Fragment切换方法的封装  
1. 提供多栈管理（官方的fragment是单栈的）  
2. 简化Fragment的管理  
3. 完全模仿Activity的接口方法和回调（如startFragment()、finish()等）  
4. 通过Intent启动，通过onFragmentResult()方法回调  
5. ___NEW___ 使用Rx的方式启动Fragment: startFragmentForObservable(Intent intent)  
6. ___NEW___ 自定义Fragment切换动画

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
    compile 'com.github.Yumenokanata:FragmentManager:2.0.1b'
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
