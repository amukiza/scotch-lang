# Java Bindings

Because what good is a JVM language when you can't use Java with it?

Scotch allows for bindings to tie Java code into the language itself. There
is an annotation-driven API provided as well as an implicit form that must be
followed in order to provide Java definitions for Scotch.

## Binding Values

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

import scotch.symbol.Value;     // marks a method as a declared value
import scotch.symbol.ValueType; // marks a method as a type for a declared value

// This class can be named anything, and you can have multiple classes in this package
// having Scotch definitions.
public class JavaDefinitions {

	// Value methods must return a curried lambda, the method name itself is arbitrary.
	// A thunk should always be ultimately returned after all arguments are applied to
	// a function to ensure lazy evaluation. Everything is a "Callable" until it's
	// needed.
	@Value(memberName = "secondElement")
	public static Applicable<Tuple2<A, B>, B> secondElement() {
		return applicable(tuple -> callable(() -> tuple.call().into((a, b) -> b)));
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
