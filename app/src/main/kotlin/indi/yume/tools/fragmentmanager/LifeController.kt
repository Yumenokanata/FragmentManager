package indi.yume.tools.fragmentmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

/**
 * Created by yume on 18-3-12.
 */


interface ActivityLifecycleOwner {
    val lifeSubject: Subject<ActivityLifeEvent>

    fun makeState(event: ActivityLifeEvent) = lifeSubject.onNext(event)

    fun destroyLifecycle() = lifeSubject.onComplete()

    fun bindActivityLife(): Observable<ActivityLifeEvent> = lifeSubject

    companion object {
        fun defaultLifeSubject(): Subject<ActivityLifeEvent> =
                PublishSubject.create<ActivityLifeEvent>().toSerialized()
    }
}

interface FragmentLifecycleOwner {
    val lifeSubject: Subject<FragmentLifeEvent>

    fun makeState(event: FragmentLifeEvent) = lifeSubject.onNext(event)

    fun destroyLifecycle() = lifeSubject.onComplete()

    fun bindFragmentLife(): Observable<FragmentLifeEvent> = lifeSubject

    companion object {
        fun defaultLifeSubject(): Subject<FragmentLifeEvent> =
                PublishSubject.create<FragmentLifeEvent>().toSerialized()
    }
}


//<editor-fold desc="Activity Life">
sealed class ActivityLifeEvent {
    data class OnCreate(val activity: Activity, val savedInstanceState: Bundle?): ActivityLifeEvent()
    data class OnStart(val activity: Activity): ActivityLifeEvent()
    data class OnResume(val activity: Activity): ActivityLifeEvent()
    data class OnNewIntent(val activity: Activity, val intent: Intent?): ActivityLifeEvent()
    data class OnActivityResult(val activity: Activity, val requestCode: Int,
                                val resultCode: Int, val data: Intent?): ActivityLifeEvent()
    data class OnPause(val activity: Activity): ActivityLifeEvent()
    data class OnStop(val activity: Activity): ActivityLifeEvent()
    data class OnDestroy(val activity: Activity): ActivityLifeEvent()
}

//</editor-fold>

//<editor-fold desc="Fragment Life">
sealed class FragmentLifeEvent {
    data class OnCreate(val fragment: Fragment, val savedInstanceState: Bundle?): FragmentLifeEvent()

    data class OnCreateView(val fragment: Fragment,
                            val inflater: LayoutInflater,
                            val container: ViewGroup?,
                            val savedInstanceState: Bundle?): FragmentLifeEvent()

    data class OnViewCreated(val fragment: Fragment, val view: View?, val savedInstanceState: Bundle?): FragmentLifeEvent()
    data class OnStart(val fragment: Fragment) : FragmentLifeEvent()
    data class OnResume(val fragment: Fragment) : FragmentLifeEvent()
    data class OnPause(val fragment: Fragment) : FragmentLifeEvent()
    data class OnShow(val fragment: Fragment, val showMode: OnShowMode): FragmentLifeEvent()
    data class OnHide(val fragment: Fragment, val hideMode: OnHideMode): FragmentLifeEvent()
    data class OnDestroy(val fragment: Fragment): FragmentLifeEvent()
}
//</editor-fold>