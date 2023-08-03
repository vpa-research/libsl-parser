libsl "1.0.0";
library literalArrays;
typealias Int = int32;
typealias IntArray = array<Int>;
typealias String = string;
typealias StringArray = array<String>;
define action TEST_ACTION(
    a: IntArray
);
define action TEST_ACTION(
    s: StringArray
);
define action TEST_ACTION_ANY(
    anyParam: any
);
automaton A : Int {
    var arrayVariable: IntArray;
    fun f(param: Int) {
        action TEST_ACTION([123]);
        action TEST_ACTION([123]);
        action TEST_ACTION([123, 321]);
        action TEST_ACTION(["test string"]);
        action TEST_ACTION(["test string1", "test string2"]);
        action TEST_ACTION([]);
        action TEST_ACTION([1 + 2 + 3]);
        action TEST_ACTION_ANY(512);
    }
    fun g() {
        arrayVariable = ["1", "2", "null"];
        arrayVariable = [1, 2, 3];
    }
}