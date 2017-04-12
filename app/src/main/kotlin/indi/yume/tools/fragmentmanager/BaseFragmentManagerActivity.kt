package indi.yume.tools.fragmentmanager

import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import indi.yume.tools.fragmentmanager.event.*
import indi.yume.tools.fragmentmanager.exception.DoEffectException
import indi.yume.tools.fragmentmanager.model.ItemState
import indi.yume.tools.fragmentmanager.model.RealWorld

/**
 * Created by yume on 17-4-12.
 */

abstract class BaseFragmentManagerActivity : AppCompatActivity() {

    val stackManager: StackManager by lazy { StackManager(RealWorld(this, provideFragmentId(), supportFragmentManager)) }

    private val baseFragmentMap: Map<String, Class<*>> by lazy { baseFragmentWithTag() }

    abstract fun baseFragmentWithTag(): Map<String, Class<*>>

    @IdRes
    protected abstract fun provideFragmentId(): Int

    override fun onBackPressed() {
        stackManager.dispatch(BackAction())
    }

    open fun onBackPressed(currentStackSize: Int): Boolean = false

    fun start(builder: StartBuilder) {
        stackManager.dispatch { state ->
            val currentTag = state.currentTag
            val backItem = state.getCurrentTop()
            val backItemHashTag = backItem?.hashTag

            if (currentTag == null)
                throw DoEffectException("start new item must at a stack")

            AddAction(currentTag, ItemState(currentTag, backItemHashTag, builder))
        }
    }

    fun deleteItem(targetTag: String, hashTag: String) {
        stackManager.dispatch { state ->
            val targetItem = state.getItem(targetTag, hashTag)
            targetItem?.run { DeleteAction(targetTag, this.hashTag) } ?: EmptyAction()
        }
    }

    fun switchToStackByTag(tag: String) {
        stackManager.dispatch { state ->
            val currentTag = state.currentTag
            val defaultClass = baseFragmentMap[tag]

            SwitchAction(currentTag, tag,
                    if(defaultClass != null) ItemState.empty(tag, defaultClass) else null)
        }
    }
}