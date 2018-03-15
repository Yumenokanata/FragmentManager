package indi.yume.tools.fragmentmanager

import android.content.Intent
import android.graphics.Color
import android.support.annotation.AnimRes
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import indi.yume.tools.fragmentmanager.anno.OnHideMode
import indi.yume.tools.fragmentmanager.anno.OnShowMode
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.functions.*
import indi.yume.tools.fragmentmanager.model.AnimData
import indi.yume.tools.fragmentmanager.model.ItemState
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.ofType
import org.slf4j.LoggerFactory

/**
 * Created by yume on 17-4-11.
 *
 */

private val logger = LoggerFactory.getLogger("FragmentUtil")

typealias Transaction = Pair<ActivityItem, FragmentTransaction>

typealias CommitFun = (FragmentManager, (FragmentTransaction) -> Completable) -> Completable

val commit: CommitFun = { manager, f ->
    Completable.defer {
        val transaction = manager.beginTransaction()
        val completeable = f(transaction)
        transaction.commit()
        completeable
    }
            .subscribeOn(AndroidSchedulers.mainThread())
}

val commitNow: CommitFun = { manager, f ->
    Completable.defer {
        val transaction = manager.beginTransaction()
        val completeable = f(transaction)
        transaction.commitNow()
        completeable
    }
            .subscribeOn(AndroidSchedulers.mainThread())
}

val commitAllowingStateLoss: CommitFun = { manager, f ->
    Completable.defer {
        val transaction = manager.beginTransaction()
        val completeable = f(transaction)
        transaction.commitAllowingStateLoss()
        completeable
    }
            .subscribeOn(AndroidSchedulers.mainThread())
}

val commitNowAllowingStateLoss: CommitFun = { manager, f ->
    Completable.defer {
        val transaction = manager.beginTransaction()
        val completeable = f(transaction)
        transaction.commitNowAllowingStateLoss()
        completeable
    }
            .subscribeOn(AndroidSchedulers.mainThread())
}



fun onResume(): IO<(FragmentItem) -> Unit> =
        state { { item -> item.onShow(OnShowMode.OnResume) } }

fun onPause(): IO<(FragmentItem) -> Unit> =
        state { { item -> item.onHide(OnHideMode.OnPause) } }

fun onSwitchTag(
        foreItem: FragmentItem,
        backItem: FragmentItem): IO<() -> Unit> =
        state { {
            backItem.onHide(OnHideMode.OnSwitch)
            foreItem.onShow(OnShowMode.OnSwitch)
        } }

fun removeFragmentWithAnim(
        forePair: Pair<FragmentItem, ItemState>,
        backPair: Pair<FragmentItem, ItemState>?): IO<State<Transaction, Completable>> {
    val foreItem = forePair.first
    val foreState = forePair.second
    val backItem = backPair?.first
    val backState = backPair?.second

    val animData = foreState.animData ?: AnimData()
    if (animData.exitAnim == -1)
        return removeFragmentNoAnim(forePair, backPair)

    return state { realworld ->
        state { (activityItem, transaction) ->
            if(backItem != null)
                transaction.show(backItem.fragment.fragment)

            startAnimation(animData.exitAnim)(realworld)
                    .second(foreItem.fragment.fragment.view!!)
                    .doOnSubscribe { dis ->
                        if (backItem != null
                                && foreState.requestCode != -1
                                && foreState.isBackItem(backState))
                            backItem.onFragmentResult(foreState.requestCode,
                                    foreState.resultCode,
                                    foreState.resultData)
                    }
                    .doOnComplete {
                        activityItem.fragmentCollection.destoryData(foreItem.hashKey)

                        activityItem.activity.provideFragmentManager.beginTransaction()
                                .remove(foreItem.fragment.fragment)
                                .commit()
                        backItem?.onShow(OnShowMode.OnBack)
                    }
        }
    }
}

fun removeFragmentNoAnim(
        forePair: Pair<FragmentItem, ItemState>,
        backPair: Pair<FragmentItem, ItemState>?): IO<State<Transaction, Completable>> {
    val (foreItem, foreState) = forePair
    val (backItem, backState) = backPair ?: null to null

    return state { realworld ->
        state { (activityItem, transaction) ->
            if (backItem != null
                    && foreState.requestCode != -1
                    && foreState.isBackItem(backState))
                backItem.onFragmentResult(foreState.requestCode,
                        foreState.resultCode,
                        foreState.resultData)

            if (backItem != null)
                transaction.show(backItem.fragment.fragment)
            transaction.remove(foreItem.fragment.fragment)

            activityItem.fragmentCollection.destoryData(foreItem.hashKey)
            backItem?.onShow(OnShowMode.OnBack)

            Completable.complete()
        }
    }
}

fun showFragmentWithAnim(
        forePair: Pair<FragmentItem, ItemState>,
        backPair: Pair<FragmentItem, ItemState>): IO<State<Transaction, Completable>> {
    val (foreItem, foreState) = forePair
    val (backItem, backState) = backPair

    val animData = foreState.animData ?: AnimData()
    if (animData.enterAnim == -1)
        return showFragmentNoAnim(foreItem, backItem)

    return state { realworld ->
        state { (activityItem, transaction) ->
            transaction.show(backItem.fragment.fragment)

            foreItem.controller.bindFragmentLife()
                    .ofType<FragmentLifeEvent.OnViewCreated>()
                    .firstOrError()
                    .flatMapCompletable { event ->
                        backItem.onHide(OnHideMode.OnStartNew)
                        foreItem.onShow(OnShowMode.OnCreate)

                        val view = event.view
                        if (view == null)
                            Completable.error(DoEffectException("ViewCreate event has error: " + foreItem.fragment))
                        else
                            startAnimation(animData.enterAnim)(realworld)
                                    .second(event.view)
                    }
                    .doOnComplete {
                        activityItem.activity.provideFragmentManager
                                .beginTransaction()
                                .hide(backItem.fragment.fragment)
                                .commit()
                        backItem.onHide(OnHideMode.OnStartNewAfterAnim)
                        foreItem.onShow(OnShowMode.OnCreateAfterAnim)
                    }
        }
    }
}

fun showFragmentNoAnim(
        foreItem: FragmentItem,
        backItem: FragmentItem): IO<State<Transaction, Completable>> {
    return state { _ ->
        state { (activityItem, transaction) ->
            transaction.hide(backItem.fragment.fragment)
            transaction.show(foreItem.fragment.fragment)

            showCallback(foreItem, backItem)
        }
    }
}

fun showFragmentNoAnim(
        foreItem: FragmentItem): IO<State<Transaction, Completable>> {
    return state { _ ->
        state { (activityItem, transaction) ->
            transaction.show(foreItem.fragment.fragment)
            showCallback(foreItem)
        }
    }
}

fun showCallback(foreItem: FragmentItem, backItem: FragmentItem? = null): Completable =
        Completable.create { e ->
            foreItem.controller.bindFragmentLife()
                    .ofType<FragmentLifeEvent.OnViewCreated>()
                    .firstElement()
                    .subscribe({ _ ->
                        backItem?.onHide(OnHideMode.OnStartNew)
                        foreItem.onShow(OnShowMode.OnCreate)
                        e.onComplete()
                    }, {
                        e.onError(it)
                    })
        }

fun switchTag(
        foreItem: FragmentItem?,
        backItem: FragmentItem?): IO<State<Transaction, Completable>> {
    return state { _ ->
        state { (activityItem, transaction) ->
            Completable.create { e ->
                if (backItem != null)
                    transaction.hide(backItem.fragment.fragment)
                if (foreItem != null)
                    transaction.show(foreItem.fragment.fragment)
                e.onComplete()
            }
                    .doOnComplete {
                        backItem?.onHide(OnHideMode.OnSwitch)
                        foreItem?.onShow(OnShowMode.OnSwitch)
                    }
        }
    }
}

private fun startAnimation(@AnimRes animRes: Int): IO<(View) -> Completable> =
    state { realworld ->
        { view ->
            if (view.getBackground() == null)
                view.setBackgroundColor(Color.WHITE)

            Completable.create { e ->
                val animation = AnimationUtils.loadAnimation(view.context, animRes)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {
                        logger.trace("start anim")
                    }

                    override fun onAnimationEnd(animation: Animation) {
                        logger.trace("anim end")
                        e.onComplete()
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
                view.startAnimation(animation)
            }
        }
    }

fun getItem(item: ItemState): IO<State<Transaction, FragmentItem>> =
        getFragmentInfo(item)
                .flatMap { f ->
                    if (f != null)
                        state { state { f } }
                    else
                        putNewFragment(item)
                }

fun getFragmentInfo(state: ItemState): IO<FragmentItem?> =
        state { realWorld ->
            realWorld.activityStore.values.flatMap { it.fragmentCollection.values }
                .firstOrNull { it.hashKey == state.hashTag }
        }

fun putNewFragment(item: ItemState): IO<State<Transaction, FragmentItem>> {
    return state { realworld ->
        state { (activityItem, transaction) ->
            val fragment = item.creator.create()

            val fragmentItem = activityItem.fragmentCollection.createData(item.hashTag,
                    fragment as ManageableFragment,
                    (fragment as ManageableFragment).fragmentLifeCycle)

            transaction.add(activityItem.fragmentId, fragmentItem.fragment.fragment, fragmentItem.hashKey)

            fragmentItem
        }
    }
}

fun getClazzName(): (Intent) -> String = { it.component.className }

fun getClazz(): (String) -> Class<*> = { clazzName -> Class.forName(clazzName) }

inline fun <reified T> cast(): (Any) -> T = { o -> o as T }

fun <T> select(targetClazz: Class<T>): (Class<*>) -> Boolean =
        { clazz -> targetClazz.isAssignableFrom(clazz) }

fun getNewInstance(): (Class<*>) -> Any = { it.newInstance() }