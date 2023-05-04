libsl "1.0.0";
library simple;

typealias Int = int32;
typealias boolean = bool;

automaton A : Int {
    fun foo(x: Int, y: Int): boolean {
        result = x == y;
    }
}
