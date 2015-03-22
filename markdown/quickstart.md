# Quickstart

Scotch is still in early development, so it's very feature light. At the very least
you can get a good feel for what it's capable of.

To date, all that the compiler is capable of doing is single-file compilation.
It will run a `main` function defined within the file, and print the result to
the console. This is useful for quick iteration and debugging, and currently
used just to poke at the compiler to see what it's capable of doing given its
current state in development.

## Building The Compiler

Currently Scotch must be built in order to be used. [Clone the repo](https://github.com/lmcgrath/scotch-lang)
and run the following command within the repo folder:

```
$ ./gradlew distZip
```

This will create a zip distribution in `build/distributions/scotch-${VERSION}.zip`. Simply
unzip this file, then add the `bin` folder to your `PATH`.

## Hello World!

To run Hello World! create the following file:

```
// hello.scotch
module hello

main = "Hello World!"
```

Then in the same directory as your file, execute the following:

```
$ scotch -m hello
```

And you should see the following output:

```
main = Hello World!
```

## Running 2 + 2

In order to do things that are more complicated, many different imports are required.

A simple operation, 2 + 2, requires an import:

```
// hello.scotch
module hello
import scotch.data.num

main = 2 + 2
```

The module `scotch.data.num` brings in numeric operations `+`, `-`, `*` and a few
others that allow basic arithmetic. If you run this module you'll see the following:

```
$ scotch -m hello
$ main = 4
```

## Now Something More

### Introducing Maybe

The `Maybe` is a container for a value that may or may not be present. As long
as a value is present, we can operate on it and return the result wrapped in a
new `Maybe`. Take the following example:

```
// maybe/numbers.scotch
module maybe.numbers
import scotch.control.monad
import scotch.control.int
import scotch.data.function
import scotch.data.maybe
import scotch.data.num

actuallyNumber = Just 1
actuallyAnotherNumber = Just 2

main = do
	x <- actuallyNumber
	y <- actuallyAnotherNumber
	return $ x + y
```

Running gives:

```
$ scotch -m maybe.numbers
$ main = Just 3
```

We get a `Just 3` back because we were able to draw out the values `1` and `2`
from the `actuallyNumber` and `actuallyAnotherNumber`, operate on them and give
back the result in a new `Maybe` using the `return` function.

### More Possibilities

We can draw a value from `Just` but what happens if one of those functions gives
us back `Nothing`?

```
// maybe/numbers.scotch
module maybe.numbers
import scotch.control.monad
import scotch.control.int
import scotch.data.function
import scotch.data.maybe
import scotch.data.num
import scotch.data.ord

actuallyNumber = Just 3
probablyPositive n =
	if n >= 0
		then Just n
		else Nothing

main = do
	x <- actuallyNumber
	y <- probablyPositive $ negate 4
	return $ x * y
```

Running gives:

```
$ scotch -m maybe.numbers
$ main = Nothing
```

We get `Nothing` because our argument to `probablyPositive` was not positive, so
`Nothing` was returned rather than evaluating the expression `return $ x * y`.

## More Examples

The test code found [here](https://github.com/lmcgrath/scotch-lang/blob/master/src/test/java/scotch/compiler/steps/BytecodeGeneratorTest.java)
will show some more complicated examples you can run.

If you wish to make your module names more complicated, nest them within folders
as you would Java packages. Otherwise, feel free to continue using `hello` because
it's super easy.
