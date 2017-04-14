package indi.yume.tools.fragmentmanager

import android.view.View

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.slf4j.LoggerFactory

/**
 * Created by yume on 17-3-28.
 */

class FragmentController {
    private val logger = LoggerFactory.getLogger(FragmentController::class.java)

    private val lifecycleSubject = BehaviorSubject.create<LifeEvent>()

    fun onCreate() {
//        logger.debug("onCreate")
        lifecycleSubject.onNext(Create())
    }

    fun onViewCreated(view: View?) {
//        logger.debug("onViewCreated")
        lifecycleSubject.onNext(ViewCreated(view!!))
    }

    fun onPause() {
//        logger.debug("onPause")
        lifecycleSubject.onNext(Pause())
    }

    fun onDestroy() {
//        logger.debug("onDestroy")
        lifecycleSubject.onNext(Destroy())
//        lifecycleSubject.onComplete()
    }

    fun <T : LifeEvent> bind(eventType: Class<T>): Observable<T> {
        return lifecycleSubject
                .ofType(eventType)
    }
}

sealed class LifeEvent

class Create : LifeEvent()

data class ViewCreated(val view: View? = null) : LifeEvent()

class Pause : LifeEvent()

class Destroy : LifeEvent()
