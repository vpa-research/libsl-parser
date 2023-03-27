libsl "1.0.0";
library simple;

types {
    Int(int32);
}

automaton A : Int {
    var i: Int;

    fun f(param: Int): Int
    requires test1: param >= i;
    ensures param' < param & result == 1;
    {

    }
}