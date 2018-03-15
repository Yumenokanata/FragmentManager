package indi.yume.tools.fragmentmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.CheckResult
import android.support.v4.app.Fragment
import indi.yume.tools.fragmentmanager.event.*
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.functions.ActionTrunk
import indi.yume.tools.fragmentmanager.functions.playNull
import indi.yume.tools.fragmentmanager.functions.toTrunk
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.ManagerState
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import android.support.v4.app.ActivityCompat.startActivityForResult
import indi.yume.tools.fragmentmanager.model.AnimData


/**
 * Created by yume on 17-4-13.
 */

sealed class FragmentCreator {
    abstract fun create(): Fragment

    companion object {
        fun fromIntent(intent: Intent): FragmentCreator =
                ClassCreator(Class.forName(intent.component.className))
    }
}

data class ClassCreator(val clazz: Class<*>) : FragmentCreator() {
    override fun create(): Fragment = clazz.newInstance() as Fragment
}

data class InstantsCreator(val fragment: Fragment) : FragmentCreator() {
    override fun create(): Fragment = fragment
}


@CheckResult
fun ActivityItem.start(startBuilder: StartBuilder): Single<StateData> {
    val addFragmentAction: ActionTrunk = trunk@{ state ->
        val activityState = state.getState(hashKey) ?: return@trunk EmptyAction()

        val targetStack = startBuilder.targetStack ?: activityState.currentStack
        val backItem = activityState.getCurrentTop()
        val backItemHashTag = backItem?.hashTag

        if (targetStack == null)
            throw DoEffectException("start new item must at a stack")

        AddAction(targetStack, ItemState(targetStack, backItemHashTag, startBuilder))
    }

    return result@ if (startBuilder.newActivity != null) {
        val creator = startBuilder.creator ?: FragmentCreator.fromIntent(startBuilder.intent)
        val item = ItemState("Unuseful", null, startBuilder)

        SingleBaseActivity.startSingleActivity(context = activity.activity,
                item = item,
                activityClazz = startBuilder.newActivity!!,
                intent = startBuilder.intent,
                startActivity = { intent ->
                    if (startBuilder.requestCode != -1) {
                        startActivityForResult(activity.activity, intent, startBuilder.requestCode, null)
                    } else {
                        activity.activity.startActivity(intent)
                    }

                    if (startBuilder.isEnableAnimation) {
                        val anim = compareAnim(startBuilder.getAnim(), creator, activity.defaultAnimData())
                        activity.activity.overridePendingTransition(anim)
                    } else
                        activity.activity.overridePendingTransition(-1, -1)
                })
                .toSingle { ApplicationStore.stackManager.bind().blockingFirst() }
    } else {
        return@result ApplicationStore.stackManager.dispatch(addFragmentAction)
    }
}


fun FragmentItem.startForObservable(rxStartBuilder: RxStartBuilder): Single<Pair<Int, Bundle>> = playNull {
    val activityVal = fragment.activityItem.bind()

    RestoreManager.startFragmentForRx(hashKey, activityVal, rxStartBuilder)
} ?: Single.error(RuntimeException("Start has error"))

//fun ActivityItem.startActivityForObservable(intent: Intent): Single<Pair<Int, Bundle>> {
//    RestoreManager.startActivityForObservable(hashKey, this, rxStartBuilder)
//}


@CheckResult
fun ActivityItem.deleteItem(targetTag: String, hashTag: String): Single<StateData> {
    return ApplicationStore.stackManager.dispatch { state ->
        val activityState = state.getState(hashKey) ?: return@dispatch EmptyAction()
        val targetItem = activityState.getItem(targetTag, hashTag)

        targetItem?.run { DeleteAction(targetTag, this.hashTag) } ?: EmptyAction()
    }
}

@CheckResult
fun ActivityItem.switchToStackByTag(tag: String, defaultFragment: FragmentCreator?): Single<StateData> {
    return ApplicationStore.stackManager.dispatch { state ->
        val activityState = state.getState(hashKey) ?: return@dispatch EmptyAction()
        val currentTag = activityState.currentStack

        SwitchAction(currentTag, tag,
                if(defaultFragment != null) ItemState.empty(tag, defaultFragment) else null)
    }
}

@CheckResult
fun ActivityItem.restore(state: ManagerState): Single<StateData> {
    val activityState = state.getState(hashKey) ?: return ApplicationStore.stackManager.bind().firstOrError()

    return ApplicationStore.stackManager.dispatch(TransactionAction(listOf(
            SwitchAction(targetStack = null).toTrunk(),
            GenAction { s, _ ->
                val middleState = s.getState(hashKey) ?: return@GenAction EmptyAction()
                middleState.stackMap.nextItem()?.let { DeleteAction(it.stackTag, it.hashTag) }
            }.toTrunk(),
            TransactionAction(activityState.stackMap.allValue().map { AddAction(it.stackTag, it).toTrunk() }).toTrunk(),
            { if(activityState.currentStack != null) SwitchAction(targetStack = activityState.currentStack) else EmptyAction() }
    )))
}

fun startActivity(intent: Intent,
                  createActivity: (Intent) -> Unit): Single<ActivityKey> {
    val checkKey = random.nextLong().toString()
    intent.putExtra(INTENT_START_KEY, checkKey)
    createActivity(intent)

    return ApplicationStore.stackManager.bind()
            .distinctUntilChanged { state1, state2 -> state1.newState.activityList === state2.oldState.activityList }
            .flatMap {
                playNull {
                    val activityKey = (it.newState.activityList - it.oldState.activityList).firstOrNull().bind()
                    val activityItem = ApplicationStore.getActivityData(activityKey).bind()

                    val resultIntent = activityItem.activity.activity.intent.bind()
                    if(resultIntent.getStringExtra(INTENT_START_KEY) == checkKey) {
                        resultIntent.removeExtra(INTENT_START_KEY)
                        Observable.just(activityItem.hashKey)
                    } else {
                        Observable.empty()
                    }
                } ?: Observable.empty()
            }.firstOrError()
}

private const val INTENT_START_KEY = "intent__start_check_key"

private val random = Random()


private fun Activity.overridePendingTransition(anim: AnimData?) {
    if (anim != null && !anim.isEmpty)
        overridePendingTransition(anim.enterAnim, anim.stayAnim)
    else
        overridePendingTransition(-1, -1)
}

private fun ManageableActivity.defaultAnimData(): AnimData? {
//    return if (fragmentEnterAnim == -1 || fragmentExitAnim == -1) null else AnimData.builder()
//            .enterAnim(fragmentEnterAnim)
//            .exitAnim(fragmentExitAnim)
//            .stayAnim(activityEnterStayAnim)
//            .build()
    return if (fragmentEnterAnim == -1 || fragmentExitAnim == -1) null else AnimData().apply {
        setEnterAnim(fragmentEnterAnim)
        setExitAnim(fragmentExitAnim)
        setStayAnim(activityEnterStayAnim)
    }
}

private fun compareAnim(builderAnim: AnimData?, creator: FragmentCreator, defaultAnim: AnimData?): AnimData? {
    if (builderAnim != null && !builderAnim.isEmpty)
        return builderAnim

    val fragment = creator.create()
    if (fragment is ManageableFragment && (fragment.provideEnterAnim() != -1 || fragment.provideExitAnim() != -1))
        return AnimData().apply {
            setEnterAnim(fragment.provideEnterAnim())
            setExitAnim(fragment.provideExitAnim())
            setStayAnim(fragment.provideStayAnim())
        }

    return if (defaultAnim != null && !defaultAnim.isEmpty) defaultAnim else null
}

private fun <K, V> Map<K, List<V>>.allValue(): List<V> {
    val valueLists = values
    if(valueLists.isEmpty())
        return emptyList()

    var sumList = emptyList<V>()
    for(list in valueLists)
        sumList += list

    return sumList
}

private fun <K, V> Map<K, List<V>>.nextItem(): V? {
    val valueLists = values
    if(valueLists.isEmpty())
        return null

    for(list in valueLists)
        if(list.isNotEmpty())
            return list.last()

    return null
}