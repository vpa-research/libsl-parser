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
    implementation 'com.github.vorpal-research:libsl:Tag'
}
```
### kotlin script gradle
```
repositories {
    ...
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.vorpal-research:libsl:Tag")
}
```

Attention: do not forget to replace `Tag` to the needed version.

## Usage examples
### 1. Parse the library description (.lsl file)

```kotlin
val libSL = LibSL("some/path/to/working/dir")
val library = libSL.loadFromFile("path/to/lsl/file")
```

The constructor of LibSL() sets the path to the working dir. This dir can contain .lsl files to be imported
via `import` keyword of LibSL. If this feature isn't needed, an empty string should be passed.

After execution of this code, variable `library` will contain the ASG graph representing the description structure. 

### 2. Getting a node from ASG
For example, let's get library's name:
```kotlin
library.metadata.name
```

Also, field `metadata` contains field such as `lslVersion` and some optional fields (see sources of the 
[meta.kt](src/main/kotlin/org/jetbrains/research/libsl/asg/meta.kt))

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

The argument called `kind` sets state's kind. The value could be SIMPLE (means 'just a state'), INIT (initial state) or
FINISH (finish state). Read LibSL docs for more info.

This way to get an automaton (also functions, types and global variables) has a limitation: it can't find an automaton
if it located in `import`-ed file. There is the true-way name resolution (finding the node), see the next paragraph.

### 3. Name resolution via context
Getting the context of library:

```kotlin
val context = libSL.context
```
**Tip**: The context can be also set to LibSL object via the constructor.

Find the automaton by name:
```kotlin
val automaton = context.resolveAutomaton("MyAutomaton")
```
**Tip**: the `null` returns if no automaton found

Find type by name:
```kotlin
val type = context.resolveType("my.type")
```

There are some other functions for name resolution, see sources of 
[LslContext.kt](src/main/kotlin/org/jetbrains/research/libsl/asg/LslContext.kt).

**IMPORTANT**: when the new node is being added to the ASG and the node can be resolved via context, you must add it
to the context too:
```kotlin
context.storeResolvedAutomaton(myAutomaton)
```

**IMPORTANT**: if the name of node stored in the context is being changed, the context should be updated:

```kotlin
automaton.name = "BrandNewName"
context.storeResolvedAutomaton(automaton)
```
Optionally, the 'previous' node could be removed from the context (but this feature isn't ready yet).

### 4. Expressions
LibSL has expressions. They are used in contracts and as function's arguments. Under the hood they are represented by
[ASG nodes](src/main/kotlin/org/jetbrains/research/libsl/asg/expressions.kt).

There are some utilities to improve an expression experience:
#### 4.1. [ExpressionVisitor](src/main/kotlin/org/jetbrains/research/libsl/asg/ExpressionVisitor.kt)
This class could be used to visit expressions.

#### 4.2 [TypeInferer](src/main/kotlin/org/jetbrains/research/libsl/asg/TypeInferer.kt)
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
