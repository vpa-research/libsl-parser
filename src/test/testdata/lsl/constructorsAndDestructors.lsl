libsl "1.0.0";
library simple;

typealias Int=int32;


automaton B: Int {
    constructor B();
    destructor B();
}

automaton A : Int {

    constructor A(x: Int, y: Int) {
       x = 1;
       y = 1;
    }
    destructor A();
    fun f();
    fun g(param: Int);
}
