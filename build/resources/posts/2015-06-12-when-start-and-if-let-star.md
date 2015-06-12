---
title: if-let* 支持多个绑定的 if-let
tags: Clojure
---

经常写 Clojure 代码的人可能会发现， 常常会出现 when-let/if-let 需要
绑定多个值的时候， 并且这些值都绑定成功才行。

下面我就来提供一个版本：

```clojure

(defmacro when-let*
  "simply implementation:
  (if-let [[sym expr & more-bindings] (seq bindings)]
    `(when-let [~sym ~expr]
       (when-let* ~more-bindings ~@body))
    `(do ~@body))

  the form:
  (when-let* [a 3 b 4] (+ a b))
  will expanded as:
  (when-let [a 3]
    (when-let [b 4]
      (+ a b)"
  [bindings & body]
  (letfn [(expand [bindings]
            (if-let [[sym expr & more-bindings] (seq bindings)]
              `(when-let [~sym ~expr]
                 ~(expand more-bindings))
              `(do ~@body)))]
    (expand bindings)))

(defmacro if-let*
  ([bindings then]
   `(when-let* ~bindings ~then))
  ([bindings then else]
   (let [leted (vec (take-nth 2 bindings))
         vals (take-nth 2 (rest bindings))
         syms (vec (repeatedly (count leted) gensym))]
     `(if-let [~leted (when-let* [~@(interleave syms vals)] ~syms)]
        ~then
        ~else))))

```

更多工具看 <https://gist.github.com/gfZeng/8e8e18f148d5742b064c>