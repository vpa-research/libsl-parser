libsl "1.0.0";
library simple;

import specImportSecondary;

define action TEST_ACTION(i: Int, s: String, p: Int): Int;
define action TEST_ACTION_TWO(s: String);

automaton A : Int {
    var i: Int;

    fun f(param: Int) {
        action TEST_ACTION(1, "123", param);
        action TEST_ACTION_TWO("");
    }
}
