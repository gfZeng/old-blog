---
title: Python 实现 Clojure 中的 defmulti
tags: Clojure, Python
---

[Dynamic Dispatch][1] 是编程语言很重要的一个特性。C++ 的虚函数， Java 的重写
都提供了 [Dynamic Dispatch][1] 的机制。 Python, Ruby 之类动态语言的更不在话下。
但是她们都只提供了 Single Dispatch 的机制, 只使用一个参数做分发，也就是函数
的第一个参数(C++, Java 的 this, Python 的 self)。 C++, Java 的实例函数都
有一个隐私参数， 那就是 this。 Python 需要显式提供，是什么？当然是 self。

这些语言的 [Dynamic Dispatch][1] 机制如何实现的，在这里就不说了，改天有时间
分别讲讲他们的底层实现机制。 C++, Java 的实现机制是一样的, Python 这类
动态语言实现起来就更简单了。                

这里特别说说 Clojure， 她提供了 [Multiple Dispatch][2] 方法, 不多说，上代码。
```clojure
user> (defmulti greet :language)
;; => #'user/greet
(defmethod greet :english [{:keys [name]}]
  (str "Hello, " name))
;; => #<MultiFn clojure.lang.MultiFn@7951ec36>

(defmethod greet :chinese [{:keys [name]}]
  (str "你好，" name))
;; => #<MultiFn clojure.lang.MultiFn@7951ec36>

(defmethod greet :default [{name :name}]
  (str "Where are you come from? " name))
;; => #<MultiFn clojure.lang.MultiFn@7951ec36>

(greet {:language :english :name "Isaac"})
;; => "Hello, Isaac"

(greet {:language :chinese :name "曾高峰"})
;; => "你好，曾高峰"

(greet {:language :鸟语 :name "张三"})
;; => "Where are you come from? 张三"
``` 

我靠， 上面的 Clojure 例子并没有体现出 [Multiple Dispatch][2] 的特性，
不过没有关系， 了解 Clojure 的不用说了， 我们马上用 Python 来实现
这一机制，看完就知道。不多说， 上代码。
```python
def defmulti(switcher_fn):
    def dispatcher(*args, **kwargs):
        key = switcher_fn(*args, **kwargs)
        func = dispatcher.dispatch_map.get(key)
        if func:
            return func(*args, **kwargs)
        raise Exception("No function defined for dispatch value: %s" % key)
    dispatcher.dispatch_map = {}
    return dispatcher

def defmethod(multi_fn, key):
    def inner(wrapped):
        multi_fn.dispatch_map[key] = wrapped
        return multi_fn
    return inner

foo = defmulti(lambda x, y: x + y > 0)

@defmethod(foo, True)
def foo(x, y):
    print("x + y > 0")
        
@defmethod(foo, False)
def foo(x, y):
    print("x + y <= 0")

## test it
foo(3, 4) #=> x + y > 0                
foo(3, -4) #=> x + y <= 0                
```

上述 Python 例子中同时使用参数 x, y 做分发。lambda x, y: x + y > 0 作为选择函数，
根据 x + y 是否大于 0 来确定分发到哪一个具体实现。        

[Dynamic Dispatch][1] 这一实现可以用到很多地方，系统架构设计， 数据库设计，
其中最为常见的就是 web 编程中 url dispatcher。

我曾经展示个上述例子给我几个同事看过，有几个看完后， 抛出一个斜眼， 这个 if-else
就可以实现了。然后我沉默了， 继续写我的程序。 对，他们说的没错， 对机器来说， 都是
分支选择。但是从优雅性， 可扩展性， 易读性（尤其是大型分发系统）来说，聪明的人都看的出来。

#### BTW:
我用 Python 实现了很多 Clojure 特有的好用函数， pmap(并发性的 map), memoize, timing, once 等，
有兴趣的可以看看 <https://github.com/gfZeng/util/blob/master/python/util.py>。
once 这个 function（decorator) 值得一提， 他是特意解决 Python 丑陋的 global。有时间会单独说说。

[1]: https://en.wikipedia.org/wiki/Dynamic_dispatch    
[2]: https://en.wikipedia.org/wiki/Multiple_dispatch