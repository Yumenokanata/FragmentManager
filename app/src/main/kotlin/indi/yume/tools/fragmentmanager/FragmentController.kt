package indi.yume.tools.fragmentmanager

import android.view.View

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by yume on 17-3-28.
 */

class FragmentController {

    private val lifecycleSubject = BehaviorSubject.create<LifeEvent>()

    fun onCreate() {
        lifecycleSubject.onNext(Create())
    }

    fun onViewCreated(view: View?) {
        lifecycleSubject.onNext(ViewCreated(view))
    }

    fun onPause() {
        lifecycleSubject.onNext(Pause())
    }

    fun onDestroy() {
        lifecycleSubject.onNext(Destroy())
        lifecycleSubject.onComplete()
    }

    fun <T : LifeEvent> bind(eventType: Class<T>): Observable<T> {
        return lifecycleSubject.share()
                .ofType(eventType)
    }
}

sealed class LifeEvent

class Create : LifeEvent()

data class ViewCreated(val view: View? = null) : LifeEvent()

class Pause : LifeEvent()

class Destroy : LifeEvent()
