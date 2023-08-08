libsl "1.0.0";
library simple;

typealias Int=int32;

automaton A : Int {

    var result: Int;

    fun f(x: Int, y: Int) {
        if(x == y)
            result = x;
        else {
            y = x - y;
            result = y;
        }
    }
}
