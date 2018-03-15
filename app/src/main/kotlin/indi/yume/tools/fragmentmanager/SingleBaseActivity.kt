package indi.yume.tools.fragmentmanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import indi.yume.tools.fragmentmanager.event.SwitchAction
import indi.yume.tools.fragmentmanager.model.ItemState
import io.reactivex.Completable

/**
 * Created by yume on 18-3-14.
 */

abstract class SingleBaseActivity : DefaultAppCompatActivity() {

    override val baseFragmentWithTag: Map<String, FragmentCreator> = emptyMap()


    companion object {
        protected const val SINGLE_TAG = "tag"

        fun startSingleActivity(context: Context,
                                item: ItemState,
                                activityClazz: Class<out SingleBaseActivity>,
                                intent: Intent? = null,
                                startActivity: (Intent) -> Unit): Completable {
            return indi.yume.tools.fragmentmanager.startActivity(
                    intent?.setClass(context, activityClazz) ?: Intent(context, activityClazz),
                    startActivity)
                    .flatMap {
                        return@flatMap ApplicationStore.stackManager.dispatch(
                                SwitchAction(targetStack = SINGLE_TAG,
                                        defaultItem = item.copy(stackTag = SINGLE_TAG)))
                    }.toCompletable()
        }
    }
}