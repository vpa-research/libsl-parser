libsl "1.0.0";
library simple;
typealias Int = int32;
automaton A : Int {
    val x: Int = 1;
    val y: Int = 2;
    proc _sum(x: Int, y: Int): Int {
        result = (x + y);
    }
    proc _noReturn() {
        x = y;
    }
    fun useProc(): Int {
        _noReturn(x, y);
        result = _sum(x, y);
    }
}