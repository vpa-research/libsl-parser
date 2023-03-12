Library for building abstract semantic graph of library specification written in LibSL. 

## Getting the dependency

### groovy gradle:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
dependencies {
    implementation 'com.github.vpa-research:libsl-parser:Tag'
}
```
### kotlin script gradle
```
repositories {
    ...
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.vpa-research:libsl-parser:Tag")
}
```

Attention: do not forget to replace `Tag` to the needed version from releases(also, you can replace `Tag` with 
some commit hash from the master branch).

## Usage examples
### 1. Parse the library description (.lsl file)

```kotlin
val libSL = LibSL("some/path/to/working/dir")
val library = libSL.loadFromFile("path/to/lsl/file")
```

The constructor of LibSL() sets the path to the working dir. This dir can contain .lsl files to be imported
via `import` keyword of LibSL. If this feature isn't needed, an empty string should be passed.

After execution of this code, variable `library` will contain the LSL Tree nodes representing the description structure. 

### 2. Getting a node from the LSL Tree
For example, let's get library's name:
```kotlin
library.metadata.name
```

Also, field `metadata` contains field such as `lslVersion` and some optional fields (see sources of the 
[meta.kt](src/main/kotlin/org/jetbrains/research/libsl/nodes/meta.kt))

Let's get the list of all library's automata:
```kotlin
val automata = library.automata
```

Now, let's get the automaton with name `MyAutomaton` and add the new state to it:
```kotlin
val automaton = automata.first { automaton -> automaton.name == "MyAutomaton" }
val newState = State("MyState", kind = StateKind.SIMPLE)
automaton.states.add(newState)
```

The argument called `kind` sets state's kind. The value could be `SIMPLE` (means 'just a state'), 
`INIT` (initial state) or `FINISH` (finish state). Read LibSL docs for more info.

This way to get an automaton (also functions, types and global variables) has a limitation: it can't find an automaton
if it located in `import`-ed file. **There is the true-way name resolution (finding the node)**, see the next paragraph.

### 3. Name resolution via contexts

Contexts represent scopes of entities. They are used to describe visibility levels of such things like variables, 
automata, functions and types. For example:

```
types {
   // global context is available here
   int(int32);
   T(int32);
} 

automaton A : T {
    // global context and the A automaton context are available here
    fun foo(argA: int) {
        // global context, the A automaton context and the foo function's context are available here    
        argA = 1; // arga is in context of the foo function
    }
}

fun A.bar(argB: int) {
    // global context, the A automaton context and the bar function's context are available here    
    // error: argA = 1 as far as argA is declared in context of the function foo that isn't available here
    argB = 2; // argB is in context of the bar function
}
```

So, these contexts are pretty much like visibility scopes from other languages. 

Important note: contexts are represent like linked structures:
```kotlin
val globalScope = LslGlobalContext()
val automatonAContext = AutomatonContext(globalScope)
val functionFooContext = FunctionContext(automatonContext)
val functionBarContext = FunctionContext(automatonContext)
// NB: functionFooContext is independent of functionBarContext
```
Then, if you try to resolve the reference in context, the context will try to resolve it in itself, then in parent one, 
then in parent's parent, ...

Getting the context of library:

```kotlin
val context = libSL.context
```
**Tip**: The context can be also set to LibSL object via the constructor.

**References** are used to describe entities like automata, functions, types and variables. Each type of object has
the reference builder, so references could be got like this:
```kotlin
val intTypeRef = TypeReferenceBuilder.build("int")
val functionFooReference = FunctionReferenceBuilder.build("foo", listOf(intTypeRef), automatonAContext)
// functionFooReference describes function with name foo and one argument of type int
val intType = intType.resolve() // returns the int type or null (if it can't be resolved)
// val intType = intType.resolveOrError() returns the int type or throws an exception (if it can't be resolved)

val functionFoo = functionFooReference.resolve()
```

These movements also could be used to get an automaton or variable. So, you must use reference builders and `.resolve()`
or `resolveOrError()` functions to get nodes if it possible.

There are some other functions for name resolution, see sources of 
[references/](src/main/kotlin/org/jetbrains/research/libsl/nodes/references).

**IMPORTANT**: when the new node is being added to the ASG and the node can be resolved via context, you must add it
to the **corresponding** context too:
```kotlin
context.storeAutomaton(myAutomaton)
```

### 4. Expressions
LibSL has expressions. They are used in contracts and as function's arguments. Under the hood they are represented by
[ASG nodes](src/main/kotlin/org/jetbrains/research/libsl/nodes/expressions.kt).

There are some utilities to improve an expression experience:
#### 4.1. [ExpressionVisitor](src/main/kotlin/org/jetbrains/research/libsl/nodes/ExpressionVisitor.kt)
This class could be used to visit expressions.

#### 4.2 [TypeInferer](src/main/kotlin/org/jetbrains/research/libsl/type/TypeInferer.kt)
This class could be used to simple type resolution. Example:
```kotlin
context.typeInferer.getExpressionTypeOrNull(myExpression)
```

IMPORTANT: this class does simple type resolution. So, type of `1 + (2*4)` is `IntType`, but type of `1 + 1.0` can't be
determined. The interer also can determine types of functions, arrays and others (`1 + arr[0]`)

## Writing tests
Create the new `.lsl` file in `./src/test/testdata/lsl/`. Then run the `main()` function in 
`generateTests.kt` or run the `Generate tests` configuration preset in IntelliJ Idea. 
New test runners can be found in the file `GeneratedTests.kt`

Each ASG (abstract semantic graph) received by the parser (abstract semantic graph) is being compared with the 
result of the previous run. These results are located in `./src/test/testdata/expected/`.
