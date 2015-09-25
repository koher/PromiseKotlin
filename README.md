PromiseKotlin
==========================

_PromiseKotlin_ is a Promise library which has __similar APIs to [_PromiseK_](https://github.com/koher/PromiseK)__.

```kotlin
// `flatMap` is equivalent to `then` of JavaScript's `Promise`
val a: Promise<Int> = asyncGet(2).flatMap { asyncGet(it) }.flatMap { asyncGet(it) }
val b: Promise<Int> = asyncGet(3).map { it * it }
val sum: Promise<Int> = a.flatMap { a0 -> b.flatMap { b0 -> Promise(a0 + b0) } }

// uses Nullable for error handling
val mightFail: Promise<Int?> = asyncFailable(5).flatMap { Promise(it?.let { it * it }) }
val howToCatch: Promise<Int> = asyncFailable(7).flatMap { Promise(it ?: 0) }

// a failable operation chain with `flatMap2`
val failableChain: Promise<Int?> = asyncFailable(11).flatMap2 { it?.let { asyncFailable(it) } }
```

License
--------------------------

[The MIT License](LICENSE)
