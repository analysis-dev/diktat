# <a name="c3"></a>3. General formatting (typesetting)
<!-- =============================================================================== -->
### <a name="c3.1"></a> 3.1 File-related rules
### <a name="r3.1.1"></a> Rule 3.1.1: Avoid files that are too long. Files should not exceed 2000 lines (non-empty and non-commented lines).

If the file is too long, it is probably complex and can therefore be split into smaller files, functions, or modules.
It is recommended that you horizontally or vertically split the file according to responsibilities or hierarchy, respectively.
Code generation is the only exception to this.
Auto-generated files that are not manually modified can be longer.

### <a name="r3.1.2"></a> Rule 3.1.2: A source file contains code blocks in the following order: copyright, package name, imports, and top-level classes. They should be separated by one blank line.

a) They should be in the following order:
1.	Kdoc for licensed or copyrighted files
2.	`@file` annotation
3.	Package name
4.	Import statements
5.	Top-class header and top-function header comments
6.	Top-level classes or functions

b) Each of the preceding code blocks should be separated by a blank line. 

c) Import statements are alphabetically arranged, without using line breaks and wildcards ( wildcard imports - `*`). 

d) **Recommendation**: One `.kt` source file should contain only one class declaration, and its name should match the filename

e) Avoid empty files that do not contain the code or contain only imports/comments/package name

### <a name="s3.1.3"></a> Recommendation 3.1.3: Import statements should appear in the following order: Android, internal company imports, external imports, java core dependencies, and Kotlin standard library. Each group should be separated by a blank line.

From top to bottom, the order is the following:
1. Android
2. Imports of packages used internally in your organization
3. Imports from other non-core dependencies
4. Java core packages
5. kotlin stdlib

Each category should be alphabetically arranged. This style is compatible with  [Android import order](https://source.android.com/setup/contribute/code-style#order-import-statements).

**Valid example**:
```kotlin
import android.* // android
import androidx.* // android
import com.android.* // android

import com.your.company.* // your company's libs
import your.company.* // your company's libs

import com.fasterxml.jackson.databind.ObjectMapper // other third-party dependencies
import org.junit.jupiter.api.Assertions

import java.io.IOException // java core packages
import java.net.URL

import kotlin.system.exitProcess  // kotlin standard library
import kotlinx.coroutines.*  // official kotlin extension library
```

### <a name="s3.1.4"></a> Recommendation 3.1.4: The declaration part of class-like code structures (class, interface, etc.) should be in the following order: compile-time constants (for objects), class properties, late-init class properties, init-blocks, constructors, public methods, internal methods, protected methods, private methods, and companion object. Their declaration should be separated by blank lines.

Note:
1.	There should be no blank lines between properties. 
**Exceptions**: When there is a comment before a property on a separate line or annotations on a separate line.
2.	Properties with comments/Kdoc should be separated by a newline before the comment/Kdoc.
3.	Enum entries and constant properties (`const val`) in companion objects should be alphabetically arranged.

The declaration part of a class or interface should be in the following order:
- Compile-time constants (for objects)
- Properties
- Late-init class properties
- Init-blocks
- Constructors
- Methods or nested classes. Put nested classes next to the code they are used by.
If the classes are meant to be used externally and are not referenced inside the class, put them at the end after the companion object.
- Companion object

**Exception:**
All variants of a `(private) val` logger should be placed in the beginning of the class (`(private) val log`, `LOG`, `logger`, etc.).

<!-- =============================================================================== -->
### <a name="c3.2"></a> 3.2 Braces
### <a name="r3.2.1"></a> Rule 3.2.1: Braces must be used in conditional statements and loop blocks.

In `if`, `else`, `for`, `do`, and `while` statements, even if the program body is empty or contains only one statement, braces should be used. In special Kotlin when statements, you do not need to use braces for statements with one line. 

**Valid example:**

```kotlin
when (node.elementType) {
    FILE -> {
        checkTopLevelDoc(node)
        checkSomething()
     }
    CLASS -> checkClassElements(node)
}
```
**Exception:** The only exception is ternary operator in Kotlin (it is a single line `if () <> else <>` ) 

**Invalid example:**

```kotlin
val value = if (string.isEmpty())  // WRONG!
                0
            else
                1
```

**Valid example**: 

```kotlin
val value = if (string.isEmpty()) 0 else 1  // Okay
```

```kotlin
if (condition) {
    println("test")
} else {
    println(0)
}
```

### <a name="r3.2.2"></a> Rule 3.2.2 For *non-empty* blocks and block structures, the opening brace is placed at the end of the line

The K&R style (1TBS or OTBS) should be followed for *non-empty* code blocks with braces:
- The opening brace and first line of the code block are on the same line.
- The closing brace is on its own new line.
- The closing brace can be followed by a newline. The only exceptions are `else`, `finally`, and `while` (from `do-while` statement), or `catch` keywords.
These keywords should not be split from the closing brace by a newline.

**Exception cases**: 

1) For lambdas, there is no need to put a newline after the first (function-related) opening brace. A newline should appear only after an arrow (`->`) (see [point 5 of Rule 3.6.2](#r3.6.2)).

```kotlin
arg.map { value ->
    foo(value)
}
```

2) for `else`/`catch`/`finally`/`while` (from `do-while` statement) keywords closing brace should stay on the same line:
 ```kotlin
do {
    if (true) {
        x++
    } else {
        x--
    }
} while (x > 0) 
```
 
**Valid example:**

 ```kotlin
        return arg.map { value ->
            while (condition()) {
                method()
            }
            value 
        }

        return MyClass() {
            @Override
              fun method() {
                if (condition()) {
                    try {
                        something()
                    } catch (e: ProblemException) {
                        recover()
                    }
                } else if (otherCondition()) {
                    somethingElse()
                } else {
                    lastThing()
                }
            }
        }
 ```

<!-- =============================================================================== -->
### <a name="c3.3"></a> 3.3 Indentation
### <a name="r3.3.1"></a>Rule 3.3.1: Use spaces for indentation. Each indentation equals four spaces.

Only spaces are permitted for indentation, and each indentation should equal `4 spaces` (tabs are not permitted).
If you prefer using tabs, simply configure them to automatically change to spaces in your IDE.
These code blocks should be indented if they are placed on the new line and the following conditions are met:
-	The code block is placed immediately after an opening brace.
-	The code block is placed after each operator, including assignment operator (`+`/`-`/`&&`/`=`/etc.).
-	The code block is a call chain of methods:
```kotlin
someObject
    .map()
    .filter()
``` 
-  The code block is placed immediately the opening parenthesis.
-  The code block is placed immediately after an arrow in lambda:

 ```kotlin
arg.map { value ->
    foo(value)
}
```
    
**Exceptions**:
1.	Argument lists: \
a) Eight spaces are used to indent argument lists (both in declarations and at call sites). \
b) Arguments in argument lists can be aligned if they are on different lines. 

2.	Eight spaces are used if there is a newline after any binary operator.

3.	Eight spaces are used for functional-like styles when the newline is placed before the dot.

4.	Super type lists: \
a) Four spaces are used if the colon before the super type list is on a new line. \
b) Four spaces are used before each super type, and eight are used if the colon is on a new line. 

**Note:** there should be an indentation after all statements such as `if`, `for`, etc.; however, according to this code style, such statements require braces. 

```kotlin
if (condition)
    foo()
```

**Exceptions**: 
- When breaking parameter list of a method/class constructor it can be aligned with `8 spaces`. Or a parameter that was moved to a new line can be on the same level as the previous argument:
    
```kotlin
fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
) {   
}
```
    
- Operators like `+`/`-`/`*`/e.t.c can be indented with `8 spaces`:
    
```kotlin
val abcdef = "my splitted" +
                " string"
```
    
- List of super types should be indented with `4 spaces` if they are on different lines, or with `8 spaces` if leading colon is also on a separate line

```kotlin
class A :
    B()
    
class A
    :
        B()
```

<!-- =============================================================================== -->
### <a name="c3.4"></a> 3.4 Empty blocks
### <a name="s3.4.1"></a> Recommendation 3.4.1: Try to avoid empty blocks, and ensure braces start on a new line.

An empty code block can be closed immediately on the same line, and on the next line. However, a newline is recommended between opening and closing braces `{}`.
(See the examples below.)

**Generally, empty code blocks are prohibited** and are very bad practice (especially for catch block).
They are only appropriate for overridden functions, when the functionality of the base class is not needed in the class-inheritor.

```kotlin
override fun foo() {    
}
``` 

**Valid examples** (but note once again, that generally empty blocks are prohibited):

```kotlin
fun doNothing() {} 

fun doNothingElse() {
}
```

**Invalid examples:**
```kotlin
try {
  doSomething()
} catch (e: Some) {}
```

Use this valid code instead:
```kotlin
try {
   doSomething()
} catch (e: Some) {
}
```

<!-- =============================================================================== -->
### <a name="c3.5"></a> 3.5 Line length
### <a name="s3.5.1"></a> Recommendation 3.5.1: Line length should be less than 120 symbols.

This international code style prohibits `non-Latin` (`non-ASCII`) symbols.
(See [Rule 1.1.1: Identifiers](#r1.1.1)) However, if you still intend on using them, follow the following convention:

- One wide character occupies the width of two narrow characters.
The "wide" and "narrow" part of a character are defined by its [east asian width Unicode attribute](https://unicode.org/reports/tr11/).
Typically, narrow characters are also called "half-width" characters.
All characters in the ASCII character set include letters (such as `a, A`), numbers (such as `0, 3`), and punctuation spaces (such as `,` , `{`), all of which are narrow characters.
Wide characters are also called "full-width" characters. Chinese characters (such as `中, 文`), Chinese punctuation (`，` , `；` ), full-width letters and numbers (such as `Ａ、３`) are "full-width" characters.
Each one of these characters represents two narrow characters.

- Any line that exceeds this limit (`120 narrow symbols`) should be wrapped, as described in the [Newline section](#c3.5). 

**Exceptions:**

1.	The long URL or long JSON method reference in KDoc
2.	The `package` and `import` statements
3.	The command line in the comment, enabling it to be cut and pasted into the shell for use

<!-- =============================================================================== -->
### <a name="c3.6"></a> 3.6 Line breaks (newlines)
### <a name="s3.6.1"></a> Recommendation 3.6.1: Each line can have a maximum of one statement.
Each line can have a maximum of one code statement. This recommendation prohibits the use of code with `;`, because it worsens code visibility.

**Invalid example:**
```kotlin
val a = ""; val b = ""
```

**Valid example:**
```kotlin
val a = ""
val b = ""
```

### <a name="r3.6.2"></a>Rule 3.6.2: Rules for line-breaking

1) Compared to Java, Kotlin allows you not to put a semicolon (`;`) after each statement separated by a newline.
    There should be no redundant semicolon at the end of lines.
 
When a newline is needed to split the line, it should be placed after operators like `&&`/`||`/`+`/etc. and all infix functions (for example, `xor`).
However, the newline should be placed before operators such as `.`, `?.`, `?:`, and `::`.

Note that all operators for comparing such as `==`, `>`, `<`, e.t.c should not be split.

**Invalid example**: 
```kotlin
     if (node !=
             null && test != null) {}
```
 
**Valid example**: 
```kotlin
         if (node != null && 
                 test != null) {
         }
```
  
**Note**, that you need to follow the functional style, meaning each function call in a chain with `.` should start at a new line if the chain of functions contains more than one call:
```kotlin
  val value = otherValue!!
          .map { x -> x }
          .filter {
              val a = true
              true
          }
          .size    
```
**Note**, that parser prohibits the separation of operator `!!` from the value it is checking.

**Exception**: If a functional chain is used inside the branches of a ternary operator, it does not need to be split with newlines.

**Valid example**:  
```kotlin
if (condition) list.map { foo(it) }.filter { bar(it) } else list.drop(1)
```  
  
2)	Newlines should be placed after the assignment operator (`=`).
3)	In function or class declarations, the name of a function or constructor should not be split by a newline from the opening brace `(`.
    A brace should be placed immediately after the name without any spaces in declarations or at call sites.
4)	Newlines should be placed right after the comma (`,`).
5)	If a lambda statement contains more than one line in its body, a newline should be placed after an arrow if the lambda statement has explicit parameters.
    If it uses an implicit parameter (`it`), the newline should be placed after the opening brace (`{`).
    See the following examples.


**Invalid example:**
```kotlin
    value.map { name -> foo()
        bar()
    }
```

**Valid example:** 
```kotlin
value.map { name ->
    foo()
    bar()
}

val someValue = { node:String -> node }
```

6) When the function contains only a single expression, it can be expressed as [expression function](https://kotlinlang.org/docs/reference/functions.html#single-expression-functions). 
   The following style should not be used.
   
Instead of: 
```kotlin
override fun toString(): String { return "hi" }
```
use:
```kotlin
override fun toString() = "hi"
```

7)  If an argument list in a function declaration (including constructors) or function call contains more than two arguments, these arguments should be split by newlines in the following style.

**Valid example:** 
 ```kotlin
class Foo(val a: String,
          b: String,
          val c: String) {
}

fun foo(
        a: String,
        b: String,
        c: String
) {

}
 ```

If and only if the first parameter is on the same line as an opening parenthesis, all parameters can be horizontally aligned by the first parameter.
Otherwise, there should be a line break after an opening parenthesis.

Kotlin 1.4 introduced a trailing comma as an optional feature, so it is generally recommended to place all parameters on a separate line
and append [trailing comma](https://kotlinlang.org/docs/reference/whatsnew14.html#trailing-comma).
It makes the resolving of merge conflicts easier.

**Valid example:** 
 ```kotlin
fun foo(
        a: String,
        b: String,
) {

}
 ```

8) If super type list has more than 2 elements, they should be separated by newlines.

**Valid example:** 
```kotlin
class MyFavouriteVeryLongClassHolder :
    MyLongHolder<MyFavouriteVeryLongClass>(),
    SomeOtherInterface,
    AndAnotherOne { }
```

<!-- =============================================================================== -->
### <a name="c3.7"></a> 3.7 Blank lines
### <a name="s3.7.1"></a> Recommendation 3.7.1: Reduce unnecessary blank lines and maintain a compact code size

By reducing unnecessary blank lines, you can display more code on one screen, which in turn improves code readability.

- Blank lines should separate content based on relevance, and should be placed between groups of fields, constructors, methods, nested classes, `init` blocks, and objects (see [Rule 3.1.2](#r3.1.2)).
- Do not use more than one line inside methods, type definitions, and initialization expressions.
- Generally, do not use more than two consecutive blank lines in a row.
- Do not put newlines in the beginning or end of code blocks with curly braces.

**Valid example:**
```kotlin
fun baz() {
        
    doSomething()  // No need to add blank lines at the beginning and end of the code block
    // ...

}
```

<!-- =============================================================================== -->
### <a name="c3.8"></a> 3.8 Horizontal space
### <a name="s3.8.1"></a> Rule 3.8.1: Usage of whitespace for code separation

**Note:** This recommendation corresponds to cases where symbols are located on the same line. However, in some cases, a line break could be used instead of a space. (This logic is described in another rule.)

1.  All keywords (such as `if`, `when`, and `for`) should be separated with a single whitespace from the opening parenthesis.
    The only exception is the `constructor` keyword, which should not be separated.

2.  Separate all keywords (such as `else` or `try`) from the opening brace (`{`) with a single whitespace.
    If `else` is used in a ternary-style statement without braces, there should be a single space between `else` and the statement after: `if (condition) foo() else bar()`

3.  Use a **single** whitespace before all opening braces (`{`). The only exception is the passing of a lambda as a parameter inside parentheses:
 ```kotlin
     private fun foo(a: (Int) -> Int, b: Int) {}
     foo({x: Int -> x}, 5) // no space before '{'
 ```

4.  A single whitespace should be placed on both sides of binary operators. This also applies to operator-like symbols.
    For example: 
    
 - A colon in generic structures with the `where` keyword:  `where T : Type`
 - Arrow in lambdas: `(str: String) -> str.length()`

**Exceptions:**

- Two colons (`::`) are written without spaces:\
  `Object::toString`
- The dot separator (`.`) that stays on the same line with an object name:\
  `object.toString()`
- Safe access modifiers: `?.` and `!!`, that stay on the same line with an object name:\
  `object?.toString()`
- Operator `..` for creating ranges:\
  `1..100`

5.  Spaces should be used after (`,`), (`:`), and (`;`), except when the symbol is at the end of the line.
    However, note that this code style prohibits the use of (`;`) in the middle of a line ([Rule 3.2.2](#r3.2.2)).
    There should be no whitespaces at the end of a line.
    The only scenario where there should be no space after a colon is when the colon is used in annotation to specify a use-site target (for example, `@param:JsonProperty`).
    There should be no spaces before `,` , `:` and `;`. 
    **Exceptions** for spaces and colons are the following:
    
    - when `:` is used to separate a type and a super type, including an anonymous object (after object keyword)
    - when delegating to a superclass constructor or different constructor of the same class

**Valid example:**
```kotlin
  abstract class Foo<out T : Any> : IFoo { }
  
  class FooImpl : Foo() {
      constructor(x: String) : this(x) { /*...*/ }
      
      val x = object : IFoo { /*...*/ } 
  }
```

6. There should be *only one space* between identifier and it's type: `list: List<String>`
If the type is nullable there should be no space before `?`.

7. When using `[]` operator (`get/set`) there should be **no** spaces between identifier and `[` : `someList[0]`.

8. There should be no space between a method or constructor name (both at declaration and at call site) and a parenthesis:
   `foo() {}`. Note that this sub-rule is related only to spaces, the rules for whitespaces are described in [Rule 3.6.2](#r3.6.2).
    This rule does not prohibit, for example, the following code:
```kotlin
fun foo
(
    a: String
)
```

9. Never put a space after `(`, `[`, `<` (when used as bracket in templates) or before `)`, `]`, `>` (when used as bracket in templates).

10. There should be no spaces between prefix/postfix operator (like `!!` or `++`) and it's operand.

### <a name="s3.8.2"></a> Recommendation 3.8.2: No spaces should be inserted for horizontal alignment.

*Horizontal alignment* refers to aligning code blocks by adding space to the code. Horizontal alignment should not be used, becuase:

- When modifying code, it takes a lot of time for new developers to format, support, and fix alignment issues.
- Long identifier names will break the alignment and lead to less presentable code.
- Alignment possesses more disadvantages than advantages. To reduce maintenance costs, misalignment is the best choice.

Recommendation: Alignment only looks good for `enum class`, where it can be used in table format to improve code readability:
```kotlin
enum class Warnings(private val id: Int, private val canBeAutoCorrected: Boolean, private val warn: String) : Rule {
    PACKAGE_NAME_MISSING         (1, true,  "no package name declared in a file"),
    PACKAGE_NAME_INCORRECT_CASE  (2, true,  "package name should be completely in a lower case"),
    PACKAGE_NAME_INCORRECT_PREFIX(3, false, "package name should start from company's domain")
    ;
}
```

**Valid example:**
 ```kotlin
 private val nr: Int // no alignment, but looks fine
 private var color: Color // no alignment
 ```

**Invalid example**:
 ```kotlin
 private val    nr: Int    // aligned comment with extra spaces
 private val color: Color  // alignment for a comment and alignment for identifier name
 ```

<!-- =============================================================================== -->
### <a name="c3.9"></a> 3.9 Enumerations
### <a name="s3.9.1"></a>Recommendation 3.9.1: Enum values are separated by a comma and line break, with ';' placed on the new line.
1) Enum values are separated by comma and a line break. `;` is put on the new line:
```kotlin
enum class Warnings {
    A,
    B,
    C,
    ;
}
```

This will help resolve conflicts and reduce the value's number during merging pull requests.
Also use [trailing comma](https://kotlinlang.org/docs/reference/whatsnew14.html#trailing-comma).

2) If the enum is simple (no properties, methods, and comments inside), it can be declared in a single line:
```kotlin
enum class Suit { CLUBS, HEARTS, SPADES, DIAMONDS }
```

3) Enum classes take preference if possible, for example, instead of two Boolean properties like:

```kotlin
val isCelsius = true
val isFahrenheit = false
```

use enum class:
```kotlin
enum class TemperatureScale { CELSIUS, FAHRENHEIT }
```

- The variable value only changes within a fixed range and is defined with the enum type. 
- Avoid comparison with magic numbers of `-1, 0, and 1`, instead of this use enums:

```kotlin
enum class ComparisonResult {
    ORDERED_ASCENDING,
    ORDERED_SAME,
    ORDERED_DESCENDING,
    ;
}
```

<!-- =============================================================================== -->
### <a name="c3.10"></a> 3.10 Variable declaration
### <a name="r3.10.1"></a> Rule 3.10.1: Declare one variable per line.

Each property or variable declaration must be declared on a separate line. 

**Invalid example**:
```kotlin
val n1: Int; val n2: Int
```

### <a name="s3.10.2"></a>Recommendation 3.10.2: Variables should be declared close to the line where they are first used.
To minimize their scope, local variables should be declared close to the point where they are first used. This will also increase readability of the code.
Local variables are usually initialized during declaration or initialized immediately after.
The member fields of the class should be declared collectively (see [Rule 3.1.2](#r3.1.2) for details on class structure).

<!-- =============================================================================== -->
### <a name="c3.11"></a> 3.11 When expression
### <a name="r3.11.1"></a>Rule 3.11.1: 'when' statements must have an 'else' branch, unless the condition variable is enumerated or a sealed type.
Each `when` statement contains an `else` statement group, even if it does not contain any code.

**Exception:** If a when statement of type `enum or sealed` contains all values of a enum - there is no need to have "else" branch.
The compiler can issue a warning when it is missing.

<!-- =============================================================================== -->
### <a name="c3.12"></a> 3.12 Annotations
### <a name="s3.12.1"></a> Recommendation 3.12.1: Each annotation applied to a class, method, or constructor should be placed on its own line.

1. Annotations applied to the class, method, or constructor are placed on separate lines (one annotation per line). 

**Valid example**:
```kotlin
@MustBeDocumented
@CustomAnnotation
fun getNameIfPresent() { /* ... */ }
```

2. A single annotation should be on the same line as the code it is annotating.

**Valid example**:
```kotlin
@CustomAnnotation class Foo {}
```

3. 3.	Multiple annotations applied to a field or property can appear on the same line as the field.

**Valid example**:
```kotlin
@MustBeDocumented @CustomAnnotation val loader: DataLoader
```

<!-- =============================================================================== -->
### <a name="c3.13"></a> 3.13 Layout of comments
### <a name="s3.13.1"></a> Recommendation 3.13.1: Block comments are at the same indentation level as the surrounding code.

Block comments are at the same indentation level as the surrounding code. See examples below.

**Valid example**:

 ```kotlin
class SomeClass {
     /*
      * This is
      * okay
      */
      fun foo() {}
}
 ```

**Suggestion**: Use `/*...*/` block comments to enable automatic formatting by IDEs.

<!-- =============================================================================== -->
### <a name="c3.14"></a> 3.14 Modifiers and constant values
### <a name="s3.14.1"></a> Recommendation 3.14.1: If a declaration has multiple modifiers, always follow proper sequence.
**Valid sequence:**

```kotlin
public / internal / protected / private
expect / actual
final / open / abstract / sealed / const
external
override
lateinit
tailrec
crossinline
vararg
suspend
inner
out
enum / annotation
companion
inline / noinline
reified
infix
operator
data
```

### <a name="s3.14.2"></a>Recommendation 3.14.2: Long numerical values should be separated by an underscore.

**Note:** Underscores make it easier to read and find errors in numeric constants.
```kotlin
val oneMillion = 1_000_000
val creditCardNumber = 1234_5678_9012_3456L
val socialSecurityNumber = 999_99_9999L
val hexBytes = 0xFF_EC_DE_5E
val bytes = 0b11010010_01101001_10010100_10010010
```

<!-- =============================================================================== -->
### <a name="c3.15"></a> 3.15 Strings
### <a name="r3.15.1"></a> Rule 3.15.1: Concatenation of Strings is prohibited if the string can fit on one line. Use raw strings and string templates instead.
Kotlin has significantly improved the use of Strings: 
[String templates](https://kotlinlang.org/docs/reference/basic-types.html#string-templates), [Raw strings](https://kotlinlang.org/docs/reference/basic-types.html#string-literals)
As such, compared to using explicit concatenation, code looks much better when proper Kotlin strings are used for short lines and you do not need to split them with newlines.

**Invalid example**:
```kotlin
val myStr = "Super string"
val value = myStr + " concatenated"
```

**Valid example**:
```kotlin
val myStr = "Super string"
val value = "$myStr concatenated"
```

### <a name="r3.15.2"></a>Rule 3.15.2: String template format
**Redundant curly braces in string templates.**

In case string template contains only one variable, - there is no need to use string template. Use this variable directly.
**Invalid example**:
```kotlin
val someString = "${myArgument} ${myArgument.foo()}"
```

**Valid example**:
```kotlin
val someString = "$myArgument ${myArgument.foo()}"
```

**Redundant string template.**
In case string template contains only one variable - there is no need to use string template. Use this variable directly.

**Invalid example**:
```kotlin
val someString = "$myArgument"
```

**Valid example**:
```kotlin
val someString = myArgument
```
