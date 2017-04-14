package indi.yume.tools.fragmentmanager

import android.support.annotation.IdRes
import android.support.v7.app.AppCompatActivity
import indi.yume.tools.fragmentmanager.event.*
import indi.yume.tools.fragmentmanager.model.ManagerState
import indi.yume.tools.fragmentmanager.model.RealWorld
import io.reactivex.functions.Consumer

/**
 * Created by yume on 17-4-12.
 */

abstract class BaseFragmentManagerActivity : AppCompatActivity() {

    val stackManager: StackManager by lazy { StackManager(RealWorld(this, provideFragmentId(), supportFragmentManager)) }

    private val baseFragmentMap: Map<String, Class<out BaseManagerFragment>> by lazy { baseFragmentWithTag() }

    abstract fun baseFragmentWithTag(): Map<String, Class<out BaseManagerFragment>>

    @IdRes
    protected abstract fun provideFragmentId(): Int

    override fun onBackPressed() {
        stackManager.dispatch(BackAction())
    }

    open fun onBackPressed(currentStackSize: Int): Boolean = false

    override fun onResumeFragments() {
        super.onResumeFragments()
        stackManager.dispatch(OnResumeAction())
    }

    override fun onPause() {
        super.onPause()
        stackManager.dispatch(OnPauseAction())
    }

    fun start(builder: StartBuilder) = stackManager.start(builder)

    fun deleteItem(targetTag: String, hashTag: String) = stackManager.deleteItem(targetTag, hashTag)

    fun switchToStackByTag(tag: String) = stackManager.switchToStackByTag(tag, baseFragmentMap[tag])

    fun restore(state: ManagerState) = stackManager.restore(state)

    fun getState(func: Consumer<ManagerState>) = stackManager.getState(func)
}