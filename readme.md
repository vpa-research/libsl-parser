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

Attention: do not forget to replace `Tag` to version

## Writing tests
Write new `.lsl` file in `./src/test/testdata/lsl/`. After it, run `main()` function in 
`generateTests.kt` or `Generate tests` run configuration preset in IntelliJ Idea. 
New test runners can be found in file `GeneratedTests.kt`

On each test received by parser ASG (abstract semantic graph) is being compared with the 
result of the previous run. These results storing in `./src/test/testdata/expected/`
