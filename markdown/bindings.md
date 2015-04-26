# Java Bindings

Because what good is a JVM language when you can't use Java with it?

Scotch allows for bindings to tie Java code into the language itself using an
annotation-driven API.

## Binding Functions

These two annotations work as a pair. The `@Value` annotation marks a method as
a function, and the `@ValueType` annotation marks a method that returns the type
information for the same function.

### Annotations Used

#### @Value

This annotation marks a method as a function. Function methods should accept zero
arguments and should return either a curried function using `Applicable`s or a
zero-args function using `Callable`. Two convenience methods exist for this:

* `scotch.runtime.RuntimeSupport.applicable`
* `scotch.runtime.RuntimeSupport.callable`

Methods marked by `@Value` are arbitrarily named, as the Scotch name is indicated
using the `@Value#memberName()` property.

#### @ValueType

The `@ValueType` annotation marks a zero-args function returning a `Type` that
describes the associated function's type. To tie the `@ValueType` method to the
associated `@Value` method, the `@ValueType#forMember()` property is used.

### Example

Binding a destructuring pattern:

```
module my.scotch.module

// the Scotch definition
secondElement (_, b) = b
```

```java
// the package name must match the Scotch module name
package my.scotch.module;

import static scotch.runtime.RuntimeSupport.applicable; // creates a single-arg function
import static scotch.runtime.RuntimeSupport.callable;   // creates a thunk
import static scotch.symbol.type.Types.fn;  // creates a single-arg function type
import static scotch.symbol.type.Types.sum; // creates a complex type
import static scotch.symbol.type.Types.var; // creates a type variable

import scotch.data.tuple.Tuple2;
import scotch.runtime.Applicable;
import scotch.symbol.Value;     // marks a method as a declared function
import scotch.symbol.ValueType; // marks a method as a type for a declared function
import scotch.symbol.type.Type;

// This class can be named anything, and you can have multiple classes in this
// package having Scotch definitions.
public class JavaDefinitions {

	// Function methods must return a curried lambda, the method name itself is
	// arbitrary. A thunk should always be ultimately returned after all arguments
	// are applied to a function to ensure lazy evaluation. Everything is a
	// "Callable" until it's needed.
	@Value(memberName = "secondElement")
	public static Applicable<Tuple2<A, B>, B> secondElement() {
		return applicable(tuple -> callable(() -> tuple.call().get_1()));
	}

	// The value type must be described, the method name, again, is arbitrary.
	@ValueType(forMember = "secondElement")
	public static Type secondElement$type() {
		// Describes the type "scotch.data.tuple.(a, b) -> b"
		return fn(sum("scotch.data.tuple.(,)", asList(var("a"), var("b"))), var("b"));
	}
}
```

## Binding Data Types

Data types are declared as classes with their constructors declared as inner
child classes. The supporting functions for each constructor must also be
defined, or the constructors will be unusable.

### Annotations Used

In addition to `@Value` and `@ValueType`, Scotch uses the following annotations
to mark classes and properties:

#### @DataType

This annotation marks a class as a data type. The `@DataType#memberName()`
property gives the Scotch name of the data type, and an array of `@TypeParameter`s
provided to the `@DataType#parameters()` gives the list of generic parameters the
data type requires, which can be empty if the data type requires no parameters.

#### @TypeParameter

The `@TypeParameter` annotation is used in the `@DataType#parameters()` property
to indicate type parameter names.

#### @TypeParameters

Marks a method which returns a list of `Type`s describing the type of each parameter
in the list provided by `@DataType#memberName()` property. Generally, these types
will be type variables, but may sometimes be type variables with context constraints:

|                            | Scotch        | Java Binding             |
|----------------------------|---------------|--------------------------|
| Type Variable              | `a`           | `var("a")`               |
| Type Variable With Context | `(Eq a) => a` | `var("a", asList("Eq"))` |

#### @DataConstructor

Data constructor classes are children of classes marked with `@DataType`. Their
Scotch name is provided with `@DataConstructor#memberName()`, their associated
data type with `@DataConstructor#dataType()`. To ensure absolute ordering of
declaration within their data types, they also note their order using
`@DataConstructor#ordinal()`.

#### @DataField

Each field within a data constructor that is intended to be exposed to Scotch
should have a getter. Those getters are annotated with `@DataField` and named
using `@DataField#memberName()`. Like data constructors, fields must be absolutely
ordered, and this is done using `@DataField#ordinal()`.

#### @DataFieldType

This annotation provides the `Type` of its associated `@DataField`. The relationship
between the two is made using the `@DataFieldType#forMember()` property.

### Example

Binding to data types and constructors is quite involved. The Java code below
illustrates what is required to support the equivalent Scotch code:

```
// Scotch declaration for Maybe
module scotch.data.maybe

data Maybe something = Nothing | Just { value :: something }
```

```java
// The package name must match the Scotch module name
package scotch.data.maybe;

import static java.util.Arrays.asList;
import static scotch.runtime.RuntimeSupport.applicable;   // creates a single-arg function
import static scotch.runtime.RuntimeSupport.callable;     // creates a thunk
import static scotch.runtime.RuntimeSupport.flatCallable; // creates a thunk that directly returns a thunk
import static scotch.symbol.type.Types.fn;  // creates a single-arg function type
import static scotch.symbol.type.Types.sum; // creates a complex type
import static scotch.symbol.type.Types.var; // creates a type variable

import java.util.List;
import java.util.Objects;
import scotch.runtime.Applicable;     // creates a single-arg function
import scotch.runtime.Callable;       // creates a thunk
import scotch.symbol.DataConstructor; // marks a class as a data type constructor
import scotch.symbol.DataField;       // marks a getter for any constructors fields
import scotch.symbol.DataFieldType;   // marks a method as providing the type of a constructors field
import scotch.symbol.DataType;        // marks a class as a data type
import scotch.symbol.TypeParameter;   // marks a named type parameter
import scotch.symbol.TypeParameters;  // marks a method as providing the list of type parameters for a data type
import scotch.symbol.Value;     // marks a method as a declared function
import scotch.symbol.ValueType; // marks a method as a type for a declared function
import scotch.symbol.type.Type;


import static java.util.Arrays.asList;
import static scotch.runtime.RuntimeSupport.applicable; // creates a single-arg function
import static scotch.runtime.RuntimeSupport.callable;   // creates a thunk
import static scotch.symbol.type.Types.fn;  // creates a single-arg function type
import static scotch.symbol.type.Types.sum; // creates a complex type
import static scotch.symbol.type.Types.var; // creates a type variable

import scotch.symbol.DataConstructor; // marks a class as a constructor
import scotch.symbol.DataType;        // marks a class as a data type
import scotch.symbol.TypeParameter;   // marks a type parameter name
import scotch.symbol.TypeParameters;  // marks a method as providing type descriptors
                                      // for a data types type parameters
import scotch.symbol.Value;           // marks a method as a declared value
import scotch.symbol.ValueType;       // marks a method as a type for a declared value

@SuppressWarnings("unused")
@DataType(memberName = "Maybe", parameters = {
    @TypeParameter(name = "a"),
})
public abstract class Maybe<A> {

    // this method lists the parameters' type descriptors, which are mapped with
    // the list of parameters given with the @DataType annotation
    @TypeParameters
    public static List<Type> parameters() {
        return asList(var("a"));
    }

    // ----------------- Supporting Methods For Nothing --------------------- //

    // Constants are recommended to be stored as singleton thunks
    private static final Callable<Maybe> NOTHING = callable(Nothing::new);

    // The no-args Nothing constructor function
    @Value(memberName = "Nothing")
    public static <A> Callable<Maybe<A>> nothing() {
        return (Callable) NOTHING;
    }

    // The type of the Nothing function
    @ValueType(forMember = "Nothing")
    public static Type nothing$type() {
        return sum("scotch.data.maybe.Maybe", var("a"));
    }

    // ----------------- Supporting Methods For Just ------------------------ //

    // The one-arg Just function
    @Value(memberName = "Just")
    public static <A> Applicable<A, Maybe<A>> just() {
        return applicable(value -> callable(() -> new Just<>(value)));
    }

    // The type of the Just constructor function
    @ValueType(forMember = "Just")
    public static Type just$type() {
        return fn(var("a"), sum("scotch.data.maybe.Maybe", var("a")));
    }

    // All constructors should implement equals(), hashCode(), and toString()
    public abstract boolean equals(Object o);

    public abstract int hashCode();

    public abstract String toString();

    // ---------------- The data constructor for Nothing -------------------- //
    // Note how the data type must be indicated in order to tie the relationship
    // between the Maybe type and the Nothing constant constructor. The ordinal
    // must be provided to ensure absolute ordering of constructors within the
    // Maybe data type.
    @DataConstructor(ordinal = 0, memberName = "Nothing", dataType = "Maybe")
    public static class Nothing<A> extends Maybe<A> {

        // ------------- equals(), hashCode(), and toString() --------------- //
        // These methods in constants should only do enough to ensure any
        // constant is equivalent to the same constant.

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof Nothing;
        }

        @Override
        public int hashCode() {
            return Objects.hash(17);
        }

        @Override
        public String toString() {
            return "Nothing";
        }
    }

    // ------------------ The data constructor for Just --------------------- //
    @DataConstructor(ordinal = 1, memberName = "Just", dataType = "Maybe")
    public static class Just<A> extends Maybe<A> {

        private final Callable<A> value;

        private Just(Callable<A> value) {
            this.value = value;
        }

        // Fields are bound by annotating their getters. All fields intended to
        // be available to Scotch should have a getter method.
        @DataField(memberName = "value", ordinal = 0)
        public Callable<A> getValue() {
            return value;
        }

        // Each field type should be provided using annotated static methods
        @DataFieldType(forMember = "value")
        public static Type value$type() {
            return var("a");
        }

        // ------------- equals(), hashCode(), and toString() --------------- //
        // Note that each field value must be eagerly evaluated using call(),
        // otherwise thunks and functions are compared rather than values.

        @Override
        public boolean equals(Object o) {
            return o == this || o instanceof Just && Objects.equals(value.call(), ((Just) o).value.call());
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "Just " + value.call() + "";
        }
    }
}
```

## Binding Type Classes

### Annotations Used

### Example

```java

```

## Binding Type Instances

###  Annotations Used

### Example

```java

```
