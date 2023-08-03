libsl "1.0.0";
library simple;
typealias Byte = int8;
typealias Short = int16;
typealias Int = int32;
typealias Long = int64;
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
    fun intSum(): Int {
        result = 2 + 2;
    }
    fun int8Sum(): Byte {
        result = 2b + 3b;
    }
    fun int64Sum(): Long {
        result = 2L + 2L;
    }
}