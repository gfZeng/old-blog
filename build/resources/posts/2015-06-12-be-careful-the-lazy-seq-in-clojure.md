---
title: 当心 Clojure 的 lazy-seq
tags: Clojure
---

Clojure 这门语言的优雅之处自然离不开 lazy sequence， clojure.core 核心库里面
很多函数返回值是 lazy-seq（比如 map, filter)。

#### lazy sequence 的好处

1. 先一睹为快， 使用 lazy-cat 定义 Fibonacci 数列。
```clojure
(def fib-seq (lazy-cat [0 1] (map + fib-seq (rest fib-seq))))
```

2. 结合 map
```clojure
[x y z ,,,] => [[x 0] [y 1] [z 2] ,,,]
;; (range) 返回无穷整数序列， 如果直接获取他的值， 会内存溢出。
(defn conj-position [xs]
  (map #(do [%1 %2]) xs (range)))
```

#### lazy sequence 的缺陷（不能算是缺陷，是特性）

1. 带走异常
```clojure
;; 定义时候不发生异常， 使用的时候发生异常， 使用的时候一定注意。
;; 有时会抛出莫名奇妙的错误， 找不到出错点。
user>  (def a (for [i (range 10)] (/ 1 i)))
;; => #'user/a
user> a
ArithmeticException Divide by zero  clojure.lang.Numbers.divide (Numbers.java:156)
user> 
```

2. binding 上下文环境失效
```clojure
;; map 返回 lazy sequence， 到使用时才运算， 在这个例子中，计算
;; vs 的值的时候， 已经脱离了 (binding [*x* 7]) 的环境.
user> (def ^:dynamic *x* 3)
;; => #'user/*x*
user> 
user> (def vs (binding [*x* 7] (map #(+ *x* %) (range 10))))
;; => #'user/vs
user> vs
;; => (3 4 5 6 7 8 9 10 11 12)
;;
;; mapv 返回的不是 lazy sequence
user> (def vs (binding [*x* 7] (mapv #(+ *x* %) (range 10))))
;; => #'user/vs
user> vs
;; => [7 8 9 10 11 12 13 14 15 16]
;;
;; let 绑定动态  *x* 到 lexical's variable x, 形成闭包。 
user> (def vs (binding [*x* 7] (let [x *x*] (map #(+ x %) (range 10)))))
;; => #'user/vs
user> vs
;; => (7 8 9 10 11 12 13 14 15 16)
user> 
```