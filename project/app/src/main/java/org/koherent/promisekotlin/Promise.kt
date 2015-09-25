package org.koherent.promisekotlin

import java.util.ArrayList

private open class Optional<T> private constructor() {
    public class None<T> : Optional<T>() {}
    public class Some<T>(val value: T) : Optional<T>() {}
}

public class Promise<T> {
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
            if (value is Optional.None) {
                value = Optional.Some(it)

                for (handler in handlers) {
                    handler(it)
                }
                handlers.clear()
            }
        }
    }

    private fun reserve(handler: (T) -> Unit) {
        val v = value
        if (v is Optional.Some) {
            handler(v.value)
        } else {
            handlers.add(handler)
        }
    }

    public fun map<U>(f: (T) -> U): Promise<U> {
        return flatMap { Promise<U>(f(it)) }
    }

    public fun flatMap<U>(f: (T) -> Promise<U>): Promise<U> {
        return Promise<U> { resolve -> reserve { resolve(f(it)) } }
    }

    public fun apply<U>(f: Promise<(T) -> U>): Promise<U> {
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

public fun <T> Promise<Promise<T>>.flatten(): Promise<T> {
    return flatMap { it }
}

public fun <T : Any, U : Any> Promise<T?>.flatMap2(f: (T?) -> Promise<U?>?): Promise<U?> {
    return flatMap { f(it) ?: Promise<U?>(null) }
}

public fun <T ,U> Promise<(T) -> U>.apply2(x: Promise<T>): Promise<U> {
    return x.apply(this)
}