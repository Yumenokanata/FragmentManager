package indi.yume.tools.fragmentmanager

import android.support.annotation.IdRes
import indi.yume.tools.fragmentmanager.event.Action
import indi.yume.tools.fragmentmanager.event.EmptyAction
import indi.yume.tools.fragmentmanager.model.ManagerState
import indi.yume.tools.fragmentmanager.model.RealWorld
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.internal.schedulers.NewThreadScheduler
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit

/**
 * Created by yume on 17-4-10.
 */

class StackManager(val realWorld: RealWorld) {

    internal val subject = PublishSubject.create<(ManagerState) -> Action>().toSerialized()

    init {
        subject.toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(NewThreadScheduler())
                .scan<StateData>(StateData(ManagerState.empty(), ManagerState.empty(), EmptyAction()))
                { stateData, trunk ->
                    logger.trace("befor current state: {}", stateData)

                    try {
                        val action = trunk(stateData.newState)
                        val newStateData = StateData(stateData.newState, reduce(stateData.newState, action), action)

                        if(middleware(newStateData.oldState, newStateData.newState, newStateData.event)
                                .blockingAwait(20, TimeUnit.SECONDS)) {
                            newStateData
                        } else {
                            logger.error("Deal effect event timeout: {}", action)
                            stateData
                        }
                    } catch (e: Exception) {
                        logger.error("Deal event error: ", e)
                        stateData
                    }
                }
                .doOnNext { state -> logger.debug("current state: {}", state) }
                .subscribe({ newState -> }) { t -> logger.error("Deal event error: ", t) }
    }

    constructor(activity: BaseFragmentManagerActivity,
                @IdRes fragmentId: Int) : this(RealWorld(activity, fragmentId,
            activity.supportFragmentManager)) {
    }

    private fun reduce(oldState: ManagerState, action: Action): ManagerState {
        return action.reduce(oldState)
    }

    private fun middleware(oldState: ManagerState, newState: ManagerState, action: Action): Completable {
        return action.effect(realWorld, oldState, newState)
    }

    fun dispatch(action: Action) {
        subject.onNext { action }
    }

    fun dispatch(trunk: (ManagerState) -> Action) {
        subject.onNext(trunk)
    }

    fun unsubscribe() {
        subject.onComplete()
    }

    data class StateData(
        val oldState: ManagerState,
        val newState: ManagerState,
        val event: Action
    )

    companion object {
        private val logger = LoggerFactory.getLogger(StackManager::class.java)
    }
}