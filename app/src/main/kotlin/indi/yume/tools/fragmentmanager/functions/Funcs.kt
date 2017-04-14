package indi.yume.tools.fragmentmanager.functions

import indi.yume.tools.fragmentmanager.event.Action
import indi.yume.tools.fragmentmanager.model.ManagerState
import indi.yume.tools.fragmentmanager.model.RealWorld

/**
 * Created by yume on 17-4-11.
 */

typealias State<S, T> = (S) -> Pair<S, T>
typealias IO<T> = State<RealWorld, T>
typealias ActionTrunk = (ManagerState) -> Action

fun <S, T, R> State<S, T>.map(run: (T) -> R): State<S, R> = { state -> state to run(this(state).second) }

fun <S, T, R> State<S, T>.flatMap(run: (T) -> State<S, R>): State<S, R> = { state -> run(this(state).second)(state) }

fun <S, T> state(run: (S) -> T): State<S, T> = { state -> state to run(state) }

fun (() -> Action).toTrunk(): ActionTrunk = { this() }

fun Action.toTrunk(): ActionTrunk = { this }
