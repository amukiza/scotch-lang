# Syntax

Scotch borrows a great deal of syntax from Haskell, but also adds some of its
own as well. This guide outlines most of what currently exists in the language.

## Function Application

Functions are curried to accept only single arguments at a time. As a consequence,
the syntax to apply a function to an argument requires only separation by whitespace
or parentheses between the function and argument. This means no superfluous parentheses
or commas!

| Scotch   | C-based Languages |
|----------|-------------------|
| fn b     | fn(b)             |
| fn b c   | fn(b, c)          |
| fn (b c) | fn(b(c))          |
| fn b(c)  | fn(b, c)          |

[Functions](#syntax-values-function-literals) and [patterns](#syntax-values-patterns) are both curried.

## Declared Values

Values may be declared with a name and referenced from within other values. The
order of declaration is not important.

```
// constant expression consisting of Toast object
myFavoriteToast = Toast { burnLevel = 2, kind = Sourdough }

// capturing pattern
identity x = x

// capturing function literal
identity = \x -> x

// destructuring pattern
secondElement (_, b) = b

// destructuring function literal
secondElement = \(_, b) -> b

// constant expression using two declarations
isItTrue? = identity myFavoriteToast == myFavoriteToast
```

## Type Signatures

Declared values may be explicitly typed if necessary. This allows enforcement of
interfaces and compiler support in cases when types are undecidable. Type signatures
consist of the name of the declaration followed by a double-colon and the type
signature itself.

```
// precede the declaration with the signature
secondElement :: (a, b) -> b
secondElement (_, b) = b
```

When two declarations reference each other and both do not have a value signature,
then one must be explicitly typed or the compiler will not be able to infer the types
of either declaration (halting problem).

```
// these two values' types can't be determined because they depend on each other
fn a = fn2 a
fn2 b = fn b
```

## Function Literals

Function literals start with a backslash because it looks like a lambda (as in
Lambda Calculus) then list the arguments of the function. An arrow separates
the arguments from the body of the function. Function literals may be placed
anywhere a function value is expected.

```
// an example function which squares its argument
\x -> x * x
```

## Patterns

Patterns function similarly to functions in that they accept arguments and return
values. The key difference is that they also function as conditionals by matching
on values and can pull apart objects (destructuring).

```
// fibonacci sequence using value matching
nthFibonacci 0 = 0
nthFibonacci 1 = 1
nthFibonacci n = fib (n - 1) + fib (n - 2)

// destructuring 2-tuple objects,
// capturing properties with variable names and ignoring properties with underscores
firstElement (a, _) = a
secondElement (_, b) = b
```

### Pattern Literals

Pattern literals look almost exactly like literal functions, except their arguments
can be replaced with destructuring and ignored pattern matches.

```
// grabbing the first element of a 2-tuple and ignoring the second
\(firstElement, _) -> firstElement

// ignoring the only argument of a function
\_ -> 2
```
