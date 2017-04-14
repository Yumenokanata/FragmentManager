package indi.yume.tools.fragmentmanager

import android.support.annotation.IdRes
import indi.yume.tools.fragmentmanager.event.Action
import indi.yume.tools.fragmentmanager.event.EmptyAction
import indi.yume.tools.fragmentmanager.event.GenAction
import indi.yume.tools.fragmentmanager.event.TransactionAction
import indi.yume.tools.fragmentmanager.functions.ActionTrunk
import indi.yume.tools.fragmentmanager.model.ManagerState
import indi.yume.tools.fragmentmanager.model.RealWorld
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.internal.schedulers.NewThreadScheduler
import io.reactivex.subjects.PublishSubject
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by yume on 17-4-10.
 */

class StackManager(val realWorld: RealWorld) {

    internal val subject = PublishSubject.create<ActionTrunk>().toSerialized()

    init {
        subject.toFlowable(BackpressureStrategy.BUFFER)
                .observeOn(NewThreadScheduler())
                .scan<StateData>(StateData(ManagerState.empty(), ManagerState.empty(), EmptyAction()))
                { stateData, trunk ->
                    try {
                        handAction(stateData, trunk)
                    } catch (e: Exception) {
                        logger.error("Deal event error: ", e)
                        stateData
                    }
                }
                .doOnNext { state -> logger.debug("current state: {}", state) }
                .subscribe({ newState -> }) { t -> logger.error("Deal event error: ", t) }
    }

    constructor(activity: BaseFragmentManagerActivity,
                @IdRes fragmentId: Int) : this(RealWorld(activity, fragmentId, activity.supportFragmentManager))

    private fun handAction(stateData: StateData, trunk: ActionTrunk): StateData {
        val (oldState, newState, event) = stateData
        val action = trunk(newState)

        return when(action) {
            is TransactionAction -> {
                var newS = stateData
                for (act in action.list)
                    newS = handAction(newS, act)
                newS
            }
            is GenAction -> {
                var act = action.initAction
                var newS = stateData
                do {
                    newS = handAction(newS, act)
                    act = action.generator(newS.newState, newS.event)
                            ?.let { { _: ManagerState -> it } } ?: break
                } while (true)
                newS
            }
            else -> {
                logger.trace("befor current state: {}", newState)
                logger.trace("do action {}", action)

                val newStateData = StateData(newState, reduce(newState, action), action)

                if(middleware(newStateData.oldState, newStateData.newState, newStateData.event)
                        .blockingAwait(100, TimeUnit.SECONDS)) {
                    newStateData
                } else {
                    throw TimeoutException("Deal effect event timeout: ${action}")
                    stateData
                }
            }
        }
    }

    private fun reduce(oldState: ManagerState, action: Action): ManagerState {
        return action.reduce(oldState)
    }

    private fun middleware(oldState: ManagerState, newState: ManagerState, action: Action): Completable {
        return action.effect(realWorld, oldState, newState)
    }

    fun dispatch(action: Action) {
        dispatch { action }
    }

    fun dispatch(trunk: ActionTrunk) {
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