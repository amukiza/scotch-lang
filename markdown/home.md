# Scotch Language Spec

"Thought of it while drinking"

## Inspiration

Scotch takes great inspiration from [Haskell](http://www.haskell.org), however
it does not attempt to port Haskell to the JVM (check out
[Frege](https://github.com/Frege/frege) for that.) Scotch also does not attempt
to solve any existing problems, and only tries to bring a functional-only language
to the JVM.

## Project Purpose

Scotch arose out of frustration from years of working with Java and other
C/C++-like programming languages.

Scotch takes the bits from C-like languages that are tough:

- Verbose syntax
- Lengthy type signatures
- Difficulty composing functions together even when functions supported as - first-class citizens
- Long, hard to read conditionals
- Subtype polymorphism
- Abusing instanceof and typeof operators
- So many curly braces and parentheses and semicolons!
- Mutability by default
- Side-effect-driven state changes

And tries to alleviate them with:

- Terse syntax having little in the way of reserved keywords and operators
- Type inferencing with little need to provide type annotations
- Short, easy to understand type signatures
- First-class functions and function currying
- Pattern matching for easy conditionals and destructuring of values
- Closed types coupled with pattern matching alleviating any need for instanceof and typeof
- Ad-hoc polymorphism support by using Haskell-style [type classes](http://learnyouahaskell.com/types-and-typeclasses)
- First-class functions to support function composition
- Whitespace-separated functions and arguments in place of parentheses and commas
- [Off-side rule](http://en.wikipedia.org/wiki/Off-side_rule) syntax to remove need for curly braces and semicolons
- All values are immutable, special references to values may be mutable (as in [Clojure](http://blog.jayfields.com/2011/04/clojure-state-management.html))
