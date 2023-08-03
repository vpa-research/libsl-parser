libsl "1.0.0";
library simple;

typealias Int=int32;
typealias String=string;

define action TEST_ACTION(x: Int, s: String, p: Int, sum: Int): Int;
define action TEST_ACTION(x: Int, p: Int, sum: Int): Int;
// define action TEST_ACTION_TWO(s: String);
define action LIST_GET(aList: array<any>, itemIndex: int32): any;
define action LIST_GET(itemIndex: int32, aList: array<any>): any;

automaton A : Int {
    var i: Int;

    fun f(param: Int) {
      action TEST_ACTION(1, "123", 2, 1 + 2);
      action TEST_ACTION(1, 2, 123);
      // action TEST_ACTION_TWO("foo");
      var a: array<any>;
      action LIST_GET(a, 1);
      action LIST_GET(1, a);
    }


}
