libsl "1.0.0";
library literalArrays;

typealias Int = int32;
typealias ArrayType = array<Int>;

define action TEST_ACTION(a: ArrayType);

automaton A : Int {
    var arrayVariable: ArrayType;

    fun f(param: Int) {
        action TEST_ACTION([123]);
        action TEST_ACTION([123]);
        action TEST_ACTION([123, 321]);
        action TEST_ACTION(["test string", param]);
        action TEST_ACTION(["test string", param]);
        action TEST_ACTION([]);
        action TEST_ACTION([((1 + 2) + 3)]);
    }

    fun g() {
        arrayVariable = ["1", "2", "null", ]; // the literal list has an `array<any>` type
        arrayVariable = [1, 2, 3]; // the literal list has an `array<int>` type
    }
}
