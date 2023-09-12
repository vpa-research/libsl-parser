libsl "1.0.0";
library simple;

typealias Int = int32;
typealias Float = float32;
typealias Double = float64;
typealias V = *void;

automaton A : Int {
    var i: V;

    fun f(param: Int): V {
    }

    fun sum(): Float {
        result = 0.1f + 0.2f;
    }

    fun sumAgain(): Double {
        result = 0.1 + 0.2;
    }

    fun newSum(): Float {
        result = 0.0f + 0.1f;
    }
}
