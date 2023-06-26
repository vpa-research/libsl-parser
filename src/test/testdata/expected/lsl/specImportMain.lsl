libsl "1.0.0";
library simple;
import specImportSecondary;
typealias Int = int64;
define action TEST_ACTION(
    i: Int,
    s: `<UNRESOLVED_TYPE>`,
    p: Int
): Int;
define action TEST_ACTION_TWO(
    s: `<UNRESOLVED_TYPE>`
);
automaton A : Int {
    var i: Int;
    fun f(param: Int) {
        action TEST_ACTION(1, "123", param);
        action TEST_ACTION_TWO("");
    }
}