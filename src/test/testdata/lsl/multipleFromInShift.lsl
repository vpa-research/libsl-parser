libsl "1.0.0";
library simple;

types {
    Int(int32);
}

automaton A : Int {
    var i: Int;

    state s1, s2, s3;

    shift (s1, s2) -> s3 by f(Int);

    fun f(param: Int);
}