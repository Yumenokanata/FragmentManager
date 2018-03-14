package indi.yume.tools.fragmentmanager

import android.support.annotation.CheckResult
import android.support.annotation.IdRes
import android.support.v4.app.FragmentTransaction
import indi.yume.tools.fragmentmanager.event.Action
import indi.yume.tools.fragmentmanager.event.EmptyAction
import indi.yume.tools.fragmentmanager.event.GenAction
import indi.yume.tools.fragmentmanager.event.TransactionAction
import indi.yume.tools.fragmentmanager.functions.ActionTrunk
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.*
import io.reactivex.internal.schedulers.NewThreadScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Created by yume on 17-4-10.
 */

class StackManager : Store<StateData, ActionTrunk>(
        initState = StateData(ManagerState.empty(), ManagerState.empty(), EmptyAction()),
        reducer = { state, action -> handAction(Single.just(state), action) }) {
    @CheckResult
    fun dispatch(action: Action): Single<StateData> =
            dispatch { action }

    companion object {
        private fun reduce(oldState: ManagerState, action: Action): ManagerState {
            return action.reduce(oldState)
        }

        private fun middleware(oldState: ManagerState, newState: ManagerState, action: Action): Completable {
            return action.effect(ApplicationStore, oldState, newState)
        }

        private fun handAction(stateData: Single<StateData>, trunk: ActionTrunk): Single<StateData> {
            return stateData.flatMap<StateData> { inputStateData ->
                val action = trunk(inputStateData.newState)

                when (action) {
                    is TransactionAction -> {
                        Observable.fromIterable(action.list)
                                .reduce(Single.just(inputStateData), { newSS, act ->
                                    newSS.hide().flatMap { handAction(Single.just(it), act) }
                                }).flatMap { it }
                    }
                    is GenAction -> {
                        val subject = BehaviorSubject.createDefault<ActionTrunk>(action.initAction).toSerialized()

                        subject.reduce(inputStateData, { oldState, act ->
                            handAction(Single.just(oldState), act)
                                    .map { newState ->
                                        val newAct = action.generator(newState.newState, newState.event)
                                        if (newAct != null)
                                            subject.onNext { newAct }
                                        else
                                            subject.onComplete()

                                        newState
                                    }
                                    .blockingGet()
                        })
                    }
                    else -> {
                        val newStateData = StateData(inputStateData.newState, reduce(inputStateData.newState, action), action)

                        middleware(newStateData.oldState, newStateData.newState, newStateData.event)
                                .toSingleDefault(newStateData)
                    }
                }
            }
        }
    }
}

sealed class Free<A>

class Return<A>(val a: A) : Free<A>()
class Suspend<A>(val s: Single<A>) : Free<A>()
class FlatMap<A, B>(val s: Free<A>, val f: (A) -> Free<B>) : Free<B>()

fun <A> run(a: Free<A>): Single<A> =
        when(a) {
            is Return<A> -> Single.just(a.a)
            is Suspend<A> -> a.s
            is FlatMap<*, A> -> doFlatmap(a)
        }

inline fun <A, B> doFlatmap(a: FlatMap<A, B>): Single<B> =
        when(a.s) {
            is Suspend<A> -> a.s.s.flatMap { run(a.f(it)) }
            else -> throw RuntimeException("Impossible, since `step` eliminates these cases")
        }


data class StateData(
        val oldState: ManagerState,
        val newState: ManagerState,
        val event: Action
)


interface Reducer<S, A> {
    fun reduce(state: S, action: A): Single<S>
}

typealias LambdaAction<S> = (S) -> S

open class Store<S, A>(
        initState: S? = null,
        val reducer: (S, A) -> Single<S>
) {
    constructor(
            initState: S? = null,
            reducer: Reducer<S, A>): this(initState, reducer::reduce)

    val stateSubject: Subject<S> = BehaviorSubject.createDefault<S>(initState).toSerialized()

    init {
        bind().subscribe(
                { println("next: $it") },
                { println("error: $it") },
                { println("complete") }
        )
    }

    fun bind(): Observable<S> = stateSubject

    @CheckResult
    fun dispatch(action: A): Single<S> =
            stateSubject.firstOrError()
                    .flatMap { reducer(it, action) }
                    .doOnSuccess { stateSubject.onNext(it) }

    fun dispatchUnsafe(action: A) =
            dispatch(action).subscribe()

    @CheckResult
    fun dispatchLambda(action: LambdaAction<S>): Single<S> =
            stateSubject.firstOrError()
                    .map { action(it) }
                    .doOnSuccess { stateSubject.onNext(it) }

    fun dispatchUnsafe(action: LambdaAction<S>) =
            dispatchLambda(action).subscribe()

    fun disposable() = stateSubject.onComplete()
}