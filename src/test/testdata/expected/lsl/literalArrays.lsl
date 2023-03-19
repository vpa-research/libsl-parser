libsl "1.0.0";
library literalArrays;
typealias Int = int32;
automaton A : Int {
    fun f(param: Int) {
        action TEST_ACTION([123]);
        action TEST_ACTION([123]);
        action TEST_ACTION([123, 321]);
        action TEST_ACTION(["test string", param]);
        action TEST_ACTION(["test string", param]);
        action TEST_ACTION([]);
        action TEST_ACTION([((1 + 2) + 3)]);
    }
}
