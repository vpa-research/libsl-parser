libsl "1.0.0";
library simple;

typealias Int=int32;
typealias String=string;

define action TEST_ACTION(x: Int, s: String, p: Int, sum: Int): Int;
define action TEST_ACTION_TWO(s: String);

automaton A : Int {
    var i: Int;

    fun f(param: Int) {
      action TEST_ACTION(1, "123", param, 1+123);
      action TEST_ACTION_TWO("foo");
    }
}
