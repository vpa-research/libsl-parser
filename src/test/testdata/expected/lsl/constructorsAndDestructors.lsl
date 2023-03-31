libsl "1.0.0";
library simple;
typealias Int = int32;
automaton B : Int {
    constructor b();
    destructor b();
}
automaton A : Int {
    constructor a(x: Int, y: Int);
    destructor a();
    constructor null();
    destructor null();
    fun f();
    fun g(param: Int);
}