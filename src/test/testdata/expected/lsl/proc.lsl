libsl "1.0.0";
library simple;
typealias Int = int32;
automaton A : Int {
    val x: Int = 1;
    val y: Int = 2;
    proc _sum(x: Int, y: Int): Int {
        result = x + y;
    }
    proc _noReturn() {
    }
    fun useProc(): Int {
        `this._noReturn`();
        result = `this._sum`(x, y) + 1;
    }
}