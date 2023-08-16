libsl "1.0.0";
library simple;

typealias Int=int32;

annotation Public();
annotation Something();

@Public
@Something
automaton A : Int {
    fun f(param: Int);

    fun g(param: Int)
}
