# Syntax

Scotch borrows a great deal of syntax from Haskell, but also adds some of its
own as well. This guide outlines most of what currently exists in the language.

## Identifiers

Scotch is extraordinarily liberal in what it accepts as an identifier. Identifiers
are any sequence of letters, numbers, and symbols not separated by whitespace.

Here is a short list of some valid identifiers:

- `EggsAndBacon`
- `ham-n-eggs`
- `burnedToast`
- `2+2` *yes, 2+2 is really a single identifier!*
- `really?`
- `yes!`
- `what?!`
- `prime'`
- `doublePrime''`
- `12@9--$'`

This comes with some caveats:

- `name=value` is a single identifier

### Naming Conventions

| Element         | Naming Convention |
|-----------------|-------------------|
| Type Name       | UpperCamel        |
| Function        | lowerCamel or operator symbol |
| Variables       | lowerCamel        |
| Type Variable   | single lower-case letter\* |
| Constructor     | UpperCamel        |
| Object Property | lowerCamel        |
| Operator        | Non-alpha symbols |
| Module Name     | Valid Java package name |

\* *Note: Type variables are normally single letters, though full names can be used for clarity*

## Operators

Operators in Scotch are defined as left-associative, right-associative, or
prefix, and have 0-9 precedence.

Here is a list of built-in operators:

| Name    | Associativity | Precedence |
|---------|---------------|------------|
| -       | prefix        | 9          |
| *, /    | left          | 8          |
| +, -    | left          | 7          |
| ==, /=  | left          | 5          |
| <, >    | left          | 5          |
| <=, >=  | left          | 5          |
| >>=, >> | left          | 1          |
| $       | right         | 0          |

### The $ Operator

The `$` operator is a special operator in Scotch. It takes its right operand
and applies it as the argument to the function on its left.

```
// 2 + 2 is evaluated then passed to println
println $ 2 + 2
```

Without `$` you would have to use parentheses to evaluate `2 + 2` first.

```
println (2 + 2)
```

### The >>= And >> Operators

The `>>=` and `>>` operators are borrowed from Haskell. They are used to map over
monads. The `>>=` operator draws a value from a monad, transforming it to a new
value and returns a new monad containing the value. The `>>` operator simply
returns a new monad containing a different value.

[Monads explained with pictures](http://adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html)

### Default Operators

Any identifier can be treated as an operator if it is surrounded by backticks.
It will have the same precedence as `+` and `-`.

```
2 `plus` 3
```

## Functions

Functions may be declared with a name and referenced from within other functions.
The order of declaration is not important.

Functions are declared using a name, followed by a list of whitespace-separated
arguments, an equals sign and then the expression.

```
// the identity function
identity x = x

// a function that doesn't take any arguments, it just returns a value
myFavoriteToast = Toast { burnLevel = 2, kind = Sourdough }

// a function with two arguments
multiply x y = x * y

// a no-args function using two functions from above
isItTrue? = identity myFavoriteToast == myFavoriteToast
```

### Function Invocation

Functions are curried to accept only single arguments at a time. As a
consequence, the syntax to apply a function to an argument requires only
separation by whitespace or parentheses between the function and argument. This means no superfluous parentheses or commas!

| Scotch   | C-based Languages |
|----------|-------------------|
| fn b     | fn(b)             |
| fn b c   | fn(b, c)          |
| fn (b c) | fn(b(c))          |
| fn b(c)  | fn(b, c)          |

```
// a function with two arguments
multiply x y = x * y

// partially applying the function
triple y = multiply 3

// invoking triple with 4 gives 12
triple 4

// multiplying 3 by 4 gives 12
multiply 3 4
```

The above code can be expressed using the following JavaScript:

```javascript
// declaring multiply as a curried function
var multiply = function(x) {
    return function(y) {
        return x * y;
    }
}

// partially applying multiply
var triple = multiply(3);

// invoking triple gives 12
triple(4)

// multiplying 3 by 4 gives 12
multiply(3)(4)
```

### Returning From Functions

Because function bodies are always single expressions, the result of those
expressions is returned implicitly. As such, there is no `return` keyword.

There is a function called `return` which works to inject a value into a monadic
type:

```
// signature
return :: a -> m a

// implementation for Maybe
return x = Just x
```

See [Monads explained with pictures](http://adit.io/posts/2013-04-17-functors,_applicatives,_and_monads_in_pictures.html) for more details.

### Function Type Signatures

Functions may be explicitly typed if necessary. This allows enforcement of
interfaces and compiler support in cases when types are undecidable. Type
signatures consist of the name of the function followed by a double-colon and
the type signature itself.

```
// precede the function with the signature
multiply :: Int -> Int -> Int
multiply x y = x * y
```
Arrows in the type signature indicate a function. The left side is the argument
type and the right side is the result type. Arrows are right-associative so the
signature `Int -> Int -> Int` reads as `Int -> (Int -> Int)`.

When two functions reference each other and both do not have a type signature,
then one must be explicitly typed or the compiler will not be able to infer the
types of either function (halting problem). This problem can arise in
co-recursive functions:

```
// these two functions' types can't be determined because they depend on each other

isEven? n = if n == 0
            then True
            else isOdd (n - 1)

isOdd? n = if n == 0
           then False
           else isEven (n - 1)
```

### Function Literals

Function literals start with a backslash because it looks like a lambda (λ, from
its use in Lambda Calculus) then list the arguments of the function. An arrow
separates the arguments from the body of the function. Function literals may be
placed anywhere a normal expression is expected.

```
// the identity function
\x -> x

// a function with two arguments
\x y -> x * y
```

### Pattern Matching

Patterns expand on functions to match on their argument values, structure, or
type constructor.

```
// fibonacci sequence using value matching
nthFibonacci 0 = 0
nthFibonacci 1 = 1
nthFibonacci n = nthFibonacci (n - 1) + nthFibonacci (n - 2)

// destructuring 2-tuples,
// we grab the elements we want using variable names (a or b)
// and ignore the ones we don't care about using underscores
firstElement (a, _) = a
secondElement (_, b) = b
```

Function literals also support pattern matching:

```
// destructuring a 2-tuple with a function literal
\(_, b) -> b
```

### Function As Operators

Any function can be an operator, it just needs to be declared with the following
syntax.

```
left infix 7 (+), (-)

right infix 0 ($)

prefix 9 (-)
```

### Operators And Pattern Matching

When using an operator function in a pattern, it will be shuffled with its
arguments on either side. This means that you do not list the function name
first:

```
left infix 7 (+)

(+) :: Int -> Int -> Int
x + y = addInts x y
```

## Expressions

### Lists

Lists are singly-linked and may only contain values of the same type.

```
// created with literal syntax
[1, 2, 3]

// created using constructors
1:2:3:[]
```

### Tuples

Tuples may contain 2 to 12 values of any mix of types.

```
// a 3-tuple
(1, 2, 3)

// a 4-tuple
(a, "this one's a string", False, 42)
```

### Conditionals

Conditionals are values and can be placed where any other value is
expected. The syntax is very simple and follows these forms:

```
// single-line if/else
if condition then trueCase else falseCase

// single-line compound conditional
if condition then trueCase else if otherCondition then otherCase else falseCase

// multi-line if/else
if condition
  then trueCase
  else falseCase

// multi-line compound conditional
if condition
  then trueCase
  else if otherCondition
  then otherCase
  else falseCase  
```

## Data Types

A data type in Scotch is a single umbrella over a closed number of variants,
called "constructors". Data types are declared using the `data` keyword, followed
by the *capitalized* type name, then an equals sign and a pipe-separated list of
constructors.

### Creating Data Types

Data types require a list of type constructors. Type constructors create objects
of their parent type. To create a constructor, provide a *capitalized* name, then
optionally follow it with properties enclosed in curly braces. If there are no
properties, the constructor creates constants, otherwise it will produce objects.

Note that all data type names and constructor names must be capitalized. Data
types will fail to compile otherwise. Conversely, the first letter of property
names must be lower case.

As an example, here is a [Binary Tree Map](http://pages.cs.wisc.edu/~skrentny/cs367-common/readings/Binary-Search-Trees/):

```
// A data type with both a constant constructor and a complex constructor.

data InventoryTree = InventoryLeaf // this constructor has no properties and thus is a constant
                   | InventoryNode { item :: String, // property 'item' has type 'String'
                                     count :: Int,
                                     leftBranch :: InventoryTree,
                                     rightBranch :: InventoryTree }
```

The `InventoryNode` constructor is particularly verbose with its property definitions.
In its particular case, this helps clarify what its objects will contain. However,
there are cases where objects are so simple you may not want to name their properties:

```
// A data type describing a singly-linked list of sausage links
data SausageLinks = NoMoreSausage
                  | SausageLink Sausage SausageLinks // just a list of types after the constructor name
```

When a constructor has its properties declared as a list of types, each property
is implicitly named in the order it was declared as `_0`, `_1`, etc. and may be
referenced as any other property.

#### Single Constructor Shorthand

A data type that contains a single complex constructor can leave out the equals
sign:

```
// shorthand
data Pickle { numberOfBumps :: Int, texture :: MouthFeel }

// longhand
data Pickle = Pickle { numberOfBumps :: Int, texture :: MouthFeel }
```

### Object Constructors

Objects are instantiated from [complex constructors](#syntax-data-type-definitions-creating-data-types).
There are two ways that objects can be instantiated:

#### Object Instantiation With Property Bags

Property bag instantiation requires providing a list of properties enclosed in
curly braces after the name of the constructor to create a new object. The properties
within the property bag can be in any order.

```
// The data type definitions
data Bread = Sourdough | Pumpernickle | Rye
data Toast { kind :: Bread, burnLevel :: Int }

// instantiating Toast
myFavoriteToast = Toast { burnLevel = 2, kind = Sourdough } // properties in any order! :)

```

#### Object Instantiation With Positioned Arguments

Positioned instantiation is used with objects that have unnamed or positioned
properties. Even objects with named properties can be instantiated in this manner
as long as their arguments are provided in the order they were declared.

```
// instantiating constructor with unnamed properties
data Sausage = Sausage
data SausageLinks = NoMoreSausage
                  | SausageLink Sausage SausageLinks

threeSausages = SausageLink Sausage (SausageLink Sausage (SausageLink Sausage NoMoreSausage))

// instantiating constructor with named properties without property names
data Bread = Sourdough | Pumpernickle | Rye
data Toast { kind :: Bread, burnLevel :: Int }

myLeaseFavoriteToast = Toast Pumpernickle 5
```

### Constant Constructors

Constants are [constructors](#syntax-data-type-definitions-creating-data-types)
which have no arguments. They are singleton values and are declared in their
respective data types with no properties.

```
// A complex data type consisting of three constant constructors
data Color = Red | Green | Blue

// A complex data type having both a constant constructor and a complex constructor
data SausageLinks = NoMoreSausage // a constant constructor
                  | SausageLink Sausage SausageLinks // a complex constructor
```

Using constants in an expression is really easy. You just reference them by name.

```
// A list of colors
colors = [Red, Green, Blue]

// two sausage links
twoSausages = SausageLink Sausage (SausageLink Sausage NoMoreSausage)
```

### Pattern Matching On Data Types

Data type constructors can be expanded and pulled apart so you only have to worry
about the specific parts you need.

```
data MouthFeel = Soft | Crunchy
data Pickle { numberOfBumps :: Int, texture :: MouthFeel }

// we pull apart Pickle to grab numberOfBumps as 'n'
numberOfBumps Pickle { numberOfBumps = n } = n

// we only say True if the constructor of MouthFeel is Crunchy
// otherwise we don't care what constructor is used
isCrunchy? Pickle { texture = Crunchy } = True
isCrunchy? _                            = False
```

## Do-Notation

Do-notation is borrowed directly from [Haskell](http://en.wikibooks.org/wiki/Haskell/do_notation).
It is syntactic sugar over the monad operators `>>=` and `>>`, and associated
function literal arguments.

```
// using normal syntax
actuallyNumber >>= \x -> probablyPositive (-4) >>= \y -> return $ x * y

// using do-notation
do
	x <- actuallyNumber
	y <- probablyPositive (-4)
	return $ x * y
```

## Whitespace

Scotch uses whitespace ([off-side rule](http://en.wikipedia.org/wiki/Off-side_rule))
to delimit blocks of code in a way that is similar to Haskell and F#. When
semicolons and curly braces aren't used to delimit blocks, they are inserted
automatically by using whitespace. Curly braces are inserted around `do` blocks
and `let` declarations, while semicolons are inserted at the ends of lines. Both
curly braces and semicolons are optional and are recommended to be left out.

Take note that tabs count as 8 spaces. Mixing them is not recommended and it is
encouraged that spaces be used over tabs.

### In Functions

Expressions may be broken across lines as long as all following lines are indented
further than the first line. This rule applies regardless of whether semicolons
are used:

```
fn a b = something being
  done with a and b

a >= b = if compare a b == GreaterThan
         then True
         else False
```

### In Data Declarations

Data declarations must have the last curly brace indented further than the `data`
keyword, otherwise a semicolon is inserted before the closing brace:

```
// right syntax
data Pickle {
    numberOfBumps :: Int,
    texture :: MouthFeel
  }

// wrong syntax
data Pickle {
    numberOfBumps :: Int,
    texture :: MouthFeel
}

// because this happens
data Pickle {
    numberOfBumps :: Int,
    texture :: MouthFeel
;}
```

Because of this restriction, data declarations are recommended to use syntax
similar to the following:

```
data Pickle { numberOfBumps :: Int,
              texture :: MouthFeel }
```

### In Do-Notation

Do-notation can use curly braces instead of whitespace and this be indented
arbitrarily:

```
actuallyNumber = Just 1
actuallyAnotherNumber = Just 2

expression = do {
  	x <- actuallyNumber;
	y <- actuallyAnotherNumber;
	 return $ x + y;
}
```
