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

Attention: do not forget to replace `Tag` to the needed version

## Writing tests
Create the new `.lsl` file in `./src/test/testdata/lsl/`. Then run the `main()` function in 
`generateTests.kt` or run the `Generate tests` configuration preset in IntelliJ Idea. 
New test runners can be found in the file `GeneratedTests.kt`

Each ASG (abstract semantic graph) received by the parser (abstract semantic graph) is being compared with the 
result of the previous run. These results are located in `./src/test/testdata/expected/`
