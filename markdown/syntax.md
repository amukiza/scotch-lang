# Syntax

Scotch borrows a great deal of syntax from Haskell, but also adds some of its
own as well. This guide outlines most of what currently exists in the language.

## Values

### Declared Values

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

### Type Signatures

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

### Function Literals

Function literals start with a backslash because it looks like a lambda (as in
Lambda Calculus) then list the arguments of the function. An arrow separates
the arguments from the body of the function. Function literals may be placed
anywhere a function value is expected.

```
// an example function which squares its argument
\x -> x * x
```

### Patterns

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

#### Pattern Literals

Pattern literals look almost exactly like literal functions, except their arguments
can be replaced with destructuring and ignored pattern matches.

```
// grabbing the first element of a 2-tuple and ignoring the second
\(firstElement, _) -> firstElement

// ignoring the only argument of a function
\_ -> 2
```

### Function Application

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

## Data Types

Data types describe values. They can be declared with one or more *constructors*,
each of which can have zero or more properties. When constructors have properties,
the properties may optionally be named.

```
// Toast as a record type with a single constructor having unnamed properties
data Toast Bread Int

// Toast as a record type with a single constructor having named properties
data Toast { kind Bread, // the property name 'kind' is followed by the type 'Bread'
             burnLevel Int }

// Singly-linked sausages with two constructors
data SausageLinks
    = NoSausage // NoSausage has no properties
    | SausageLink Sausage SausageLinks // SausageLink has two unnamed properties of type Sausage and SausageLinks

// French-to-Spanish dictionary as a bi-map
data FrenchSpanishDictionary
    = EmptyEntry
    | DictionaryEntry { key FrenchWord,
                        value SpanishWord,
                        left FrenchSpanishDictionary,
                        right FrenchSpanishDictionary }
```

Data types are closed types. This means they cannot be extended like types in
object-oriented languages like Java or C#. The reason for this is because you can
create [patterns](#syntax-patterns) over all known constructors within a data type, which allows for
complete function definitions.

```
// Maybe can only be either Nothing or Just
data Maybe something = Nothing | Just something
```

As a consequence of the above definition of `Maybe` the patterns below are possible
and can be analyzed by the compiler to ensure all constructors within `Maybe` are handled.

```
valueOf (Just x) = x
valueOf Nothing  = throw "Got nothin!"
```

An incomplete pattern like the one below causes the compiler to emit a warning
because there is no handling for `Nothing`:

```
valueOf (Just x) = x
// pattern for Nothing intentionally absent
```

<span style="color: red;">**WARNING:**</span> this warning feature is not yet implemented.

### Constructors

Constructors are used to create value instances of a particular data type.
Constructors themselves are not types. This is reflected in [value signatures](#syntax-values-type-signatures)
where only type names used.

```
// Using the Maybe definition
data Maybe something = Nothing | Just something

// Declare a value
just5 :: Maybe Int
just5 = Just 5
```

In the above example, even though `just5` always returns `Just 5`, we can only
give it the signature `Maybe Int`.

### Constant Constructors

Constant constructors take no arguments and are referenced by name only.

```
// the constructor Nothing is a constant
data Maybe somethings = Nothing | Just something
```

### Object Constructors

Objects consist of a collection of named properties. Even when properties are
unnamed, they are named according to ordinal (`_0`, `_1`, etc).

Objects can be initialized using either unordered property bags or by passing
arguments in the order the properties were declared.

```
// object initializer using property bag
Toast { burnLevel = 2, kind = Sourdough }

// object initializer with positioned arguments
Toast Sourdough 2
```

If using a property bag, then all properties must be present or the compiler
will emit an error listing the missing properties. Likewise, the compiler emits
an error for properties that don't exist.

### Generic Data Types

Data types may accept type arguments so any type can be stored in their affected
properties. The most common use case for type arguments are collection types.

```
// The built-in list type
data [a] = [] | a : [a] // a is the generic argument to List

// The (not yet) built-in bi-map type
data BiMap keyType valueType // keyType and valueType are generic arguments to BiMap
    = MapLeaf
    | MapBranch { key keyType,
                  value valueType,
                  left BiMap keyType valueType,
                  right BiMap keyType valueType }
```

Read [here](#syntax-lists) for the list type.

Generic type arguments are generally given using single, lower-case letters. Full
names are perfectly fine for clarity as long as the first letter is lower case.
