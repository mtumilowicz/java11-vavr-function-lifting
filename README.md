# java11-vavr-function-lifting
Examples of vavr function / `Try` lifting.

_Reference_: https://www.vavr.io/vavr-docs/#_lifting

# preface
A partial function from `X` to `Y` is a function `f: X′ → Y`, 
for some `X′ c X`.

We lift function `f` to `f′: X -> Y` is such manner:
* `f′ == f` on `X′`
* `f′(x) = Option.none()` for `x e X\X′`

# project description
1. suppose we have 