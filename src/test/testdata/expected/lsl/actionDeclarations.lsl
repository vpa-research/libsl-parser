libsl "1.0.0";
library simple;
typealias Int = int32;
annotation Something;
annotation Ann;
@Something
define action SUM(
    x: Int,
    y: Int
): Int;
@Something
define action NO_RETURN(
    x: Int,
    y: Int
);
define action NO_ARGS: Int;
automaton A : Int {
    var x: Int = 1;
    var y: Int = 2;
    fun f(param: Int): Int {
        result = action SUM(x, y);
    }
    fun v() {
        action NO_RETURN(1, 2);
    }
    fun c(): Int {
        result = action NO_ARGS();
    }
}
