package org.koherent.promisekotlin

import junit.framework.TestCase
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer
import kotlin.concurrent.timerTask
import kotlin.test.assertEquals
import kotlin.test.fail

public class PromiseTest : TestCase() {
    public fun testMap() {
        val signal = CountDownLatch(1)

        try {
            asyncGet(3).map {
                assertEquals(it, 3)
                it * it
            }.map {
                assertEquals(it, 9)
                signal.countDown()
            }

            signal.await(3L, TimeUnit.SECONDS)
        } catch(e: Exception) {
            fail(e.getMessage())
        }
    }

    public fun testFlatMap() {
        if (true) {
            val signal = CountDownLatch(1)

            try {
                asyncGet(3).flatMap {
                    assertEquals(it, 3)
                    signal.countDown()
                    Promise(Unit)
                }

                signal.await(3L, TimeUnit.SECONDS)
            } catch(e: Exception) {
                fail(e.getMessage())
            }
        }

        if (true) {
            val signal = CountDownLatch(1)

            try {
                asyncGet(3).flatMap {
                    assertEquals(it, 3)
                    asyncGet(it * it)
                }.flatMap {
                    assertEquals(it, 9)
                    signal.countDown()
                    Promise(Unit)
                }

                signal.await(3L, TimeUnit.SECONDS)
            } catch(e: Exception) {
                fail(e.getMessage())
            }
        }
    }

    public fun testFlatMap2() {
        if (true) {
            val signal = CountDownLatch(1)

            try {
                asyncGetOrFail(3, false).flatMap2 {
                    it?.let {
                        assertEquals(it, 3)
                        asyncGetOrFail(it * it, false)
                    }
                }.flatMap {
                    if (it != null) {
                        assertEquals(it, 9)
                    } else {
                        fail()
                    }
                    signal.countDown()
                    Promise(Unit)
                }

                signal.await(3L, TimeUnit.SECONDS)
            } catch(e: Exception) {
                fail(e.getMessage())
            }
        }

        if (true) {
            val signal = CountDownLatch(1)

            try {
                asyncGetOrFail(3, false).flatMap2 {
                    it?.let {
                        assertEquals(it, 3)
                        asyncGetOrFail(it * it, true)
                    }
                }.flatMap {
                    assertEquals(it, null)
                    signal.countDown()
                    Promise(Unit)
                }

                signal.await(3L, TimeUnit.SECONDS)
            } catch(e: Exception) {
                fail(e.getMessage())
            }
        }

        if (true) {
            val signal = CountDownLatch(1)

            try {
                asyncGetOrFail(3, true).flatMap2 {
                    assertEquals(it, null)
                    it?.let {
                        asyncGetOrFail(it * it, true)
                    }
                }.flatMap {
                    assertEquals(it, null)
                    signal.countDown()
                    Promise(Unit)
                }

                signal.await(3L, TimeUnit.SECONDS)
            } catch(e: Exception) {
                fail(e.getMessage())
            }
        }
    }

    public fun testApply2() {
        val signal = CountDownLatch(1)

        try {
            val foo: (Int) -> (Int) -> Foo = ::foo
            Promise(foo).apply2(asyncGet(2)).apply2(asyncGet(3)).flatMap {
                assertEquals(it.a, 2)
                assertEquals(it.b, 3)
                signal.countDown()
                Promise(Unit)
            }

            signal.await(3L, TimeUnit.SECONDS)
        } catch(e: Exception) {
            fail(e.getMessage())
        }
    }
}

fun asyncGet(value: Int): Promise<Int> {
    return Promise {
        resolve ->
        Timer().schedule(timerTask {
            resolve(Promise(value))
        }, 100L)
    }
}

fun asyncGetOrFail(value: Int, fails: Boolean): Promise<Int?> {
    return if (fails) {
        Promise<Int?>(null)
    } else {
        asyncGet(value).map {(x: Int): Int? -> x }
    }
}

fun foo(a: Int): (Int) -> Foo {
    return { b ->
        Foo(a, b)
    }
}

data class Foo(val a: Int, val b: Int)