libsl "1.0.0";
library simple;
types {
    Int(int32);
}
automaton A : Int {
    state s1;
    state s2;
    state s3;
    shift s1 -> s3(f(Int));
    shift s2 -> s3(f(Int));
    var i: Int;
    fun f(param: Int);
}