package org.koherent.promisekotlin

import java.util.ArrayList

private open class Optional<T> private constructor() {
    public class None<T> : Optional<T>() {}
    public class Some<T>(val value: T) : Optional<T>() {}
}

public class Promise<T> {
    private val lock = Object()

    private var value: Optional<T> = Optional.None()
    private var handlers: MutableList<(T) -> Unit> = ArrayList()

    public constructor(value: T) {
        this.value = Optional.Some(value)
    }

    public constructor(executor: ((Promise<T>) -> Unit) -> Unit) {
        executor { resolve(it) }
    }

    private fun resolve(promise: Promise<T>) {
        promise.reserve {
            synchronized(lock) {
                if (value is Optional.None) {
                    value = Optional.Some(it)

                    for (handler in handlers) {
                        handler(it)
                    }
                    handlers.clear()
                }
            }
        }
    }

    private fun reserve(handler: (T) -> Unit) {
        synchronized(lock) {
            val v = value
            if (v is Optional.Some) {
                handler(v.value)
            } else {
                handlers.add(handler)
            }
        }
    }

    public fun <U> map(f: (T) -> U): Promise<U> {
        return flatMap { Promise(f(it)) }
    }

    public fun <U> flatMap(f: (T) -> Promise<U>): Promise<U> {
        return Promise { resolve -> reserve { resolve(f(it)) } }
    }

    public fun <U> apply(f: Promise<(T) -> U>): Promise<U> {
        return f.flatMap { map(it) }
    }

    public override fun toString(): String {
        val v = value
        return when (v) {
            is Optional.Some -> "Promise(${v.value})"
            else -> "Promise"
        }
    }
}

public fun <T> pure(x: T): Promise<T> {
    return Promise(x)
}

public fun <T> Promise<Promise<T>>.flatten(): Promise<T> {
    return flatMap { it }
}

public fun <T : Any, U : Any> Promise<T?>.flatMap2(f: (T?) -> Promise<U?>?): Promise<U?> {
    return flatMap { f(it) ?: Promise<U?>(null) }
}

public fun <T, U> Promise<(T) -> U>.apply2(x: Promise<T>): Promise<U> {
    return x.apply(this)
}