package indi.yume.tools.fragmentmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.AnimRes
import android.support.annotation.CallSuper
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import io.reactivex.subjects.Subject

/**
 * Created by yume on 17-4-12.
 */

abstract class DefaultAppCompatActivity : AppCompatActivity(), ManageableActivity, ActivityLifecycleOwner {
    @AnimRes
    override var fragmentExitAnim: Int = R.anim.fragment_left_exit
    @AnimRes
    override var fragmentEnterAnim = R.anim.fragment_left_enter
    @AnimRes
    override var activityEnterStayAnim = R.anim.stay_anim


    override val lifeSubject: Subject<ActivityLifeEvent> = ActivityLifecycleOwner.defaultLifeSubject()

    override val activity: Activity = this

    override val activityLifeCycle: ActivityLifecycleOwner = this

    override val provideFragmentManager: FragmentManager
        get() = supportFragmentManager

    @CallSuper
    override fun onBackPressed() {
        super<ManageableActivity>.onBackPressed()
    }

    @CallSuper
    override fun onResumeFragments() {
        super<AppCompatActivity>.onResumeFragments()
        super<ManageableActivity>.onResumeFragments()
    }

    @CallSuper
    override fun onPause() {
        super<AppCompatActivity>.onPause()
        makeState(ActivityLifeEvent.OnPause(this))
        super<ManageableActivity>.onPause()
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        super<ManageableActivity>.onCreate(savedInstanceState)
        makeState(ActivityLifeEvent.OnCreate(this, savedInstanceState))

    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        makeState(ActivityLifeEvent.OnStart(this))
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        makeState(ActivityLifeEvent.OnResume(this))
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        makeState(ActivityLifeEvent.OnStop(this))
    }

    @CallSuper
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        makeState(ActivityLifeEvent.OnNewIntent(this, intent))
    }

    @CallSuper
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        makeState(ActivityLifeEvent.OnActivityResult(this, requestCode, resultCode, data))
    }

    @CallSuper
    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        super<ManageableActivity>.onDestroy()
        makeState(ActivityLifeEvent.OnDestroy(this))
        destroyLifecycle()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super<AppCompatActivity>.onSaveInstanceState(outState)
        super<ManageableActivity>.onSaveInstanceState(outState)
    }
}