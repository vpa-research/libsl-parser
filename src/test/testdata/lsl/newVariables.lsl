libsl "1.0.0";
library simple;

types {
    Int(int32);
}

automaton A (val x: Int) : Int {
    val y: Int;

    fun f(param: Int) {
        requires i == 0;
        val x: Int = 1;
        val v: Int;
        if(y > 1) {
            val b: Int;
        }
    }
}
