libsl "1.0.0";
library targetAnnotation;

types {
    Int(int32); // simple type
    A(int16);
}

annotation Target();

automaton B : Int {}

automaton A : Int {
    fun foo (@Target self: Int);
}
