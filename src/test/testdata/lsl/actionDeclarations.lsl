libsl "1.0.0";
library simple;

typealias Int=int32;

define action SUM(x: Int, y: Int): Int;
define action NO_RETURN(x: Int, y: Int);

automaton A : Int {
    var x: Int = 1;
    var y: Int = 2;

    fun f(param: Int): Int {
      action SUM(x, y);
    }

    fun v() {
      action NO_RETURN(1, 2);
    }
}