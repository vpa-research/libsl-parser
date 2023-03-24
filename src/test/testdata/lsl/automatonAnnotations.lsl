libsl "1.0.0";
library simple;

typealias Int=int32;

@Public
@Something(Int)
automaton A : Int {
    fun f(param: Int);

    fun g(param: Int)
}