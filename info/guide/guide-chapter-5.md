### <a name="c5"></a> 5. Functions
<!-- =============================================================================== -->
### <a name="c5.1"></a> 5.1 Function design
You can write clean code by gaining knowledge of how to build design patterns and avoid code smells.
You should utilize this approach, along with functional style, when you write Kotlin code. 
The concepts behind functional style are as follows: 
Functions are the smallest unit of combinable and reusable code.
They should have clean logic, **high cohesion**, and **low coupling** to effectively organize the code.
The code in functions should be simple, and should not conceal the author's original intentions.
Additionally, it should have a clean abstraction, and control statements should be used in a straightforward manner.
The side effects (code that does not affect a function's return value, but affects global/object instance variables) should not be used for state changes.
The only exceptions to this are state machines.

Kotlin is [designed](https://www.slideshare.net/abreslav/whos-more-functional-kotlin-groovy-scala-or-java) to support and encourage functional programming.
This language features built-in mechanisms that support functional programming. In addition, standard collections and sequences feature methods that enable functional programming (for example, `apply`, `with`, `let`, and `run`), Kotlin Higher-Order functions, function types, lambdas, and default function arguments.
As [previously discussed](#r4.1.3), Kotlin supports and encourages the use of immutable types, which in turn motivates programmers to write pure functions that avoid side effects and have a corresponding output for specific input. 
The pipeline data flow for the pure function comprises a functional paradigm. It is easy to implement concurrent programming when you have chains of function calls and each step features the following characteristics:
1.	Simple
2.	Verifiable
3.	Testable
4.	Replaceable
5.	Pluggable
6.	Extensible
7.	Immutable results

There can be only one side effect in this data stream, which can be placed only at the end of execution queue.

### <a name="r5.1.1"></a> Rule 5.1.1: Avoid functions that are too long. They should consist of 30 lines (non-empty and non-comment) in total.

The function should be displayable on one screen and only implement one certain logic.
If a function is too long, it often means that it is complex and be split or made more primitive.

**Exception:** Some functions that implement complex algorithms may exceed 30 lines due to aggregation and comprehensiveness.
Linter warnings for such functions **can be suppressed**. 

Even if a long function works well, new problems or bugs due to complex logic may appear once it is modified by someone else.
As such, it is recommended that you split such functions into several separated and shorter ones that are easier to manage.
This will enable other programmers to read and modify the code properly.

### <a name="r5.1.2"></a> Rule 5.1.2: Avoid deep nesting of function code blocks. It should be limited to four levels.

The nesting depth of a function's code block is the depth of mutual inclusion between the code control blocks in the function (for example: if, for, while, and when).
Each nesting level will increase the amount of effort needed to read the code because you need to remember the current "stack" (for example, entering conditional statements and loops). 
**Exception:** The nesting levels of the lambda expressions, local classes, and anonymous classes in functions are calculated based on the innermost function, and the nesting levels of enclosing methods are not accumulated.
Functional decomposition should be implemented to avoid confusing for the developer who read the code.
This will help the reader switch between context.

### <a name="r5.1.3"></a> Rule 5.1.3: Avoid using nested functions.
Nested functions create more complex function context, thereby confusing readers.
Additionally, the visibility context may not be obvious to the reader of the code.

**Invalid example**:
```kotlin
fun foo() { 
    fun nested():String { 
        return "String from nested function" 
    } 
    println("Nested Output: ${nested()}") 
} 
```  

<!-- =============================================================================== -->
### <a name="c5.2"></a> 5.2 Function arguments
### <a name="r5.2.1"></a> Rule 5.2.1: The lambda parameter of the function should be placed last in the argument list.

With a such notation, it is easier to use curly brackets, which in turn leads to code with better readability.

**Valid example**:
```kotlin
// declaration
fun myFoo(someArg: Int, myLambda: () -> Unit) {
// ...
}

// usage
myFoo(1) { 
println("hey")
}
```

### <a name="r5.2.2"></a> Rule 5.2.2: Number of parameters of function should be limited to 5

A long argument list is a [code smell](https://en.wikipedia.org/wiki/Code_smell) that leads to less reliable code.
If there are **more than five** parameters, maintenance becomes more difficult and conflicts become much more difficult to merge.
As such, it is recommended that you reduce the number of parameters.
If groups of parameters appear in different functions multiple times, these parameters are closely related and can be encapsulated into a single Data Class.
It is recommended that you use Data Classes and Maps to unify these function arguments.

### <a name="r5.2.3"></a>Rule 5.2.3 Use default values for function arguments instead of overloading them
In Java default values for function arguments are prohibited. That's why each time when it is needed to create a function with less arguments, this function should be overloaded.
In Kotlin you can use default arguments instead.

**Invalid example**:
```kotlin
private fun foo(arg: Int) {
    // ...
}

private fun foo() {
    // ...
}
``` 

**Valid example**:
```kotlin
 private fun foo(arg: Int = 0) {
     // ...
 }
``` 
