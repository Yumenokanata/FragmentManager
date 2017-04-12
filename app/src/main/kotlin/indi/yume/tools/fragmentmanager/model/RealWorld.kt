package indi.yume.tools.fragmentmanager.model

import android.support.v4.app.FragmentManager
import indi.yume.tools.fragmentmanager.BaseFragmentManagerActivity
import indi.yume.tools.fragmentmanager.StackManager
import java.util.*

/**
 * Created by yume on 17-4-12.
 */

data class RealWorld(
        val activity: BaseFragmentManagerActivity,
        val fragmentId: Int,
        val fragmentManager: FragmentManager,
        val fragmentCollection: MutableMap<String, FragmentItem> = Hashtable<String, FragmentItem>()
)