package indi.yume.tools.fragmentmanager.functions

import java.util.concurrent.CountDownLatch
import kotlin.coroutines.experimental.Continuation
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.coroutines.experimental.EmptyCoroutineContext
import kotlin.coroutines.experimental.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.experimental.intrinsics.suspendCoroutineOrReturn
import kotlin.coroutines.experimental.startCoroutine

/**
 * Created by yume on 18-3-13.
 */


fun <B> playNull(c: suspend NullableContinuation<*>.() -> B?): B? {
    val continuation = NullableContinuation<B?>()
    val wrapReturn: suspend NullableContinuation<*>.() -> B? = { c() }
    wrapReturn.startCoroutine(continuation, continuation)
    return continuation.returnValue()
}


interface BindingInContextContinuation<in T> : Continuation<T> {
    fun await(): Throwable?
}

class NullableContinuation<A: Any?>(override val context: CoroutineContext = EmptyCoroutineContext) :
        Continuation<A> {
    private lateinit var returnedValue: SingleData<A>

    fun returnValue(): A? = if(::returnedValue.isInitialized) returnedValue.t else null

    override fun resume(value: A) {
        returnedValue = SingleData(value)
    }

    override fun resumeWithException(exception: Throwable) {
        throw exception
    }

    protected fun bindingInContextContinuation(context: CoroutineContext): BindingInContextContinuation<A> =
            object : BindingInContextContinuation<A> {
                val latch: CountDownLatch = CountDownLatch(1)

                var error: Throwable? = null

                override fun await(): Throwable? = latch.await().let { error }

                override val context: CoroutineContext = context

                override fun resume(value: A) {
                    returnedValue = SingleData(value)
                    latch.countDown()
                }

                override fun resumeWithException(exception: Throwable) {
                    error = exception
                    latch.countDown()
                }
            }

    suspend fun <T: Any> T?.bind(): T = bind { this }

    suspend fun <T: Any> (() -> T?).bindIn(context: CoroutineContext): T? =
            bindIn(context, this)

    suspend fun <T: Any> bind(m: () -> T?): T = suspendCoroutineOrReturn { c ->
        val value = m()
        if(value != null)
            c.resume(value)
        COROUTINE_SUSPENDED
    }

    suspend fun <T: Any> bindIn(context: CoroutineContext, m: () -> T?): T? = suspendCoroutineOrReturn { c ->
        val monadCreation: suspend () -> A = {
            val value = m()
            if(value != null)
                c.resume(value)
            returnedValue.t
        }
        val completion = bindingInContextContinuation(context)
        returnedValue = {
            monadCreation.startCoroutine(completion)
            val error = completion.await()
            if (error != null) {
                throw error
            }
            returnedValue
        }()
        COROUTINE_SUSPENDED
    }

    private data class SingleData<T>(val t: T)
}