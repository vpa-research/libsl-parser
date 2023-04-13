libsl "1.0.0";
library simple;
types {
    Int(int32);
}
automaton A (val x: Int) : Int {
    val y: Int;
    fun f(param: Int) {
        requires (i == 0);
    }
}