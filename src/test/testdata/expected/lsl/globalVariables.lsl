libsl "1.0.0";
library simple;

types {
    Int(int32);
}
var globalInt: Int = new A(state = S)

automaton A : Int {
    state S;
    fun f(param: Int) {
        globalInt = 1;
    }
}
