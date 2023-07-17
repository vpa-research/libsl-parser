libsl "1.0.0";
library simple;
type Type1 {
    var field: Type2;
}
type Type2 {
    var arrayField: array<Type3>;
}
type Type3 {
    var field: Int;
}
types {
    Int(int32);
}
automaton A : Int {
    fun foo(arg: Type1) {
        arg.field.arrayField[0].field = 1;
    }
}
