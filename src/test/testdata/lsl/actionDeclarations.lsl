libsl "1.0.0";
library simple;

typealias Int=int32;

annotation Something;
annotation Ann;

@Something
define action SUM(@Ann x: Int, y: Int): Int;
@Something
define action NO_RETURN(@Ann x: Int, @Ann y: Int);

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
