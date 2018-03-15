package indi.yume.tools.fragmentmanager

import android.app.Activity
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.FragmentManager
import indi.yume.tools.fragmentmanager.event.*
import indi.yume.tools.fragmentmanager.model.ManagerState
import android.support.annotation.AnimRes



/**
 * Created by yume on 18-3-12.
 */

interface ManageableActivity {

    val activityItem: ActivityItem?
        get() = ApplicationStore.getActivityData(this)

    val stackState: ManagerState
        get() = ApplicationStore.stackManager.bind().blockingFirst().newState

    @get:IdRes
    val fragmentId: Int

    val activity: Activity

    val activityLifeCycle: ActivityLifecycleOwner

    val provideFragmentManager: FragmentManager

    val baseFragmentWithTag: Map<String, FragmentCreator>


    @get:AnimRes
    var fragmentExitAnim: Int
    @get:AnimRes
    var fragmentEnterAnim: Int
    @get:AnimRes
    var activityEnterStayAnim: Int

    fun setDefaultFragmentAnim(@AnimRes enterAnim: Int,
                               @AnimRes exitAnim: Int) {
        this.fragmentEnterAnim = enterAnim
        this.fragmentExitAnim = exitAnim
    }

    /**
     *
     * @param activityEnterStyAnim set anim when startNewActivity
     * [this.startFragmentOnNewActivity]
     * [this.startFragmentOnNewActivityForResult]
     * you can set 0 will not have anim (background is black),
     * this anim just work for stay old activity when start a new activity,
     * default this time is 2000ms:
     * <translate xmlns:android="http://schemas.android.com/apk/res/android" android:fromYDelta="0%p" android:toYDelta="0%p" android:duration="2000"></translate>
     * please set animation time to fit your enter anim for start new activity.
     */
    fun setDefaultFragmentAnim(@AnimRes enterAnim: Int,
                               @AnimRes exitAnim: Int,
                               @AnimRes activityEnterStyAnim: Int) {
        this.fragmentEnterAnim = enterAnim
        this.fragmentExitAnim = exitAnim
        this.activityEnterStayAnim = activityEnterStyAnim
    }


    fun start(builder: StartBuilder) = activityItem?.start(builder)?.defaultSubscribe()

    fun deleteItem(targetTag: String, hashTag: String) = activityItem?.deleteItem(targetTag, hashTag)?.defaultSubscribe()

    fun switchToStackByTag(tag: String) = activityItem?.switchToStackByTag(tag, baseFragmentWithTag[tag])?.defaultSubscribe()

    fun restore(state: ManagerState) = activityItem?.restore(state)?.defaultSubscribe()


    fun onBackPressed() {
        ApplicationStore.stackManager.dispatch(BackAction()).defaultSubscribe()
    }

    fun onBackPressed(currentStackSize: Int): Boolean = false

    fun onResumeFragments() {
        activityItem?.apply {
            ApplicationStore.stackManager.dispatch(OnResumeAction(hashKey)).defaultSubscribe()
        }
    }

    fun onCreate(savedInstanceState: Bundle?) {
        ApplicationStore.activityStore.onCreate(this, activity as ActivityLifecycleOwner, savedInstanceState)
        ApplicationStore.stackManager.dispatch(OnCreateAction(this)).defaultSubscribe()
    }

    fun onPause() {
        activityItem?.apply {
            ApplicationStore.stackManager.dispatch(OnPauseAction(hashKey)).defaultSubscribe()
        }
    }

    fun onSaveInstanceState(outState: Bundle?) {
        ApplicationStore.activityStore.onSaveInstanceState(this, outState)
    }

    fun onDestroy() {
        ApplicationStore.activityStore.destoryData(this)

        activityItem?.apply {
            ApplicationStore.stackManager.dispatch(OnDestroyAction(hashKey)).defaultSubscribe()
        }
    }
}