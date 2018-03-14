package indi.yume.tools.fragmentmanager.model

import android.content.Intent
import android.os.Bundle
import indi.yume.tools.fragmentmanager.ClassCreator
import indi.yume.tools.fragmentmanager.FragmentCreator
import indi.yume.tools.fragmentmanager.StartBuilder

/**
 * Created by yume on 17-4-10.
 */

data class ItemState(
    val creator: FragmentCreator,

    val fromIntent: Intent?,
    val animData: AnimData?,
    val stackTag: String,
    val hashTag: String,

    val backItemHashTag: String?,

    val requestCode: Int,
    val resultCode: Int,
    val resultData: Bundle?) {

    constructor(stackTag: String, creator: FragmentCreator) : this(
            creator = creator,
            fromIntent = null,
            animData = AnimData(),
            stackTag = stackTag,
            hashTag = Object().hashCode().toString(),
            backItemHashTag = null,
            requestCode = -1,
            resultCode = -1,
            resultData = null
    )

    constructor(targetTag: String,
                backItemHashTag: String?,
                builder: StartBuilder) : this(
            creator = ClassCreator(Class.forName(builder.intent.component.className)),
            fromIntent = builder.intent,
            animData = builder.anim,
            stackTag = targetTag,
            hashTag = Object().hashCode().toString(),
            backItemHashTag = backItemHashTag,
            requestCode = builder.requestCode,
            resultCode = -1,
            resultData = null
    )

    fun isBackItem(item: ItemState?): Boolean {
        if (item != null && backItemHashTag != null)
            return item.hashTag == backItemHashTag

        return false
    }

    companion object {

        fun empty(stackTag: String, backTag: String, creator: FragmentCreator): ItemState {
            return ItemState(stackTag, creator).copy(backItemHashTag = backTag)
        }

        fun empty(stackTag: String, creator: FragmentCreator): ItemState {
            return ItemState(stackTag, creator)
        }

        fun empty(stackTag: String, clazz: Class<*>): ItemState {
            return ItemState(stackTag, ClassCreator(clazz))
        }
    }
}