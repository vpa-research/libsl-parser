libsl "1.0.0";
library targetAnnotation;
types {
    Int(int32);
    A(int16);
}
annotation target;
automaton B : Int {
}
automaton A : Int {
    fun foo(@target() self: B);
}