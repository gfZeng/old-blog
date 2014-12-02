---
title: 根据航班预测旅客的旅途
tags: Clojure, Logic Programming
---

最近了解了下 Clojure 的 core.logic, 正好在网上看到这个例子， <http://jerrypeng.me/2014/11/25/playing-core-logic>，觉得这个例子比较有意思。 于是就照着作者的需求写了一个自己的版本。

得益于 Logic Programming 的优势， 实现这个预测代码非常简短，改天我再用算法实现一下这个程序和这个做对比。

#### 首先考虑乘客直达的情况
这种情况下， 乘客只需要买一个航班的票。

假设航班经过的航站为 a, b, c。 那么他的起始航站（旅程）可能的组合为 (a, b), (a, c), (b, c)。

乘客 A 乘坐的航班 F 经过的航站集为 S，那么乘客 A 的旅途 (x, y) 一定满足：
> x， y 都属于 S， x 在 y 的前面。

现在我们基于这一逻辑抽象来定义 beforeo。

```clojure
(defne beforeo [x y l]
  ([_ _ [x . ?r]] (membero y ?r))
  ([_ _ [_ . ?r]] (beforeo x y ?r)))

;; test
(run* [x y] (beforeo x y '[a b c]))
;=> ([a b] [a c] [b c])
```

#### 乘客转乘的情况
假设乘客 A 乘坐 F1 后转乘 F2， F1， F2 航站集分别为 S1， S2， 那么乘客 A 的旅途 (x, t1, y) （t1 为中转站）一定满足（这次我们用上面的定义来描述）：
```clojure
(all
  (beforeo x t1 S1)
  (beforeo t1 y S2))

;; test
(let [S1 '[a b c]
      S2 '[b d g]]
  (run* [x t1 y]
    (beforeo x t1 S1)
    (beforeo t1 y S2)))
;=> ([a b d] [a b g])
```

#### 多次转乘的时候
直接贴代码
```clojure
(def flights-db {"MU123" ["PEK" "WUH" "CAN"]
                 "MU234" ["CAN" "BKK" "SIN"]
                 "CA888" ["SIN" "LHR"]})

(defn infer-travel [flights]
  (let [vs (repeatedly (inc (count flights)) lvar)
        cs (map conj (map vec (partition 2 1 vs))
                (map #(get flights-db %) flights))]
    (run* [q]
      (== q vs)
      (everyg #(apply beforeo %) cs))))

;; test
(infer-travel ["MU123" "MU234"])
;=> (("PEK" "CAN" "BKK") ("PEK" "CAN" "SIN") ("WUH" "CAN" "BKK") ("WUH" "CAN" "SIN"))
```



如果各位感兴趣完整代码我贴在 gist 上面了： <https://gist.github.com/gfZeng/7576b52cb869aa79fdc3>
