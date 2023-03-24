libsl "1.0.0";
library simple;

typealias Int=int32;

define action SUM(x: Int, y: Int): Int;
define action NO_RETURN(x: Int, y: Int);

automaton A : Int {

    fun f(param: Int) {
      action SUM(1, 2);
    }

    fun v() {
      action NO_RETURN(1, 2);
    }
}