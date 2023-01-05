libsl "1.0.0";
library simple;

type Type1 {
    field: Type2;
}

type Type2 {
    arrayField: array<Type3>;
}

type Type3 {
    field: Int;
}

types {
    Int(int32);
}

automaton A : Int {
    fun foo(arg: Type1) {
        arg.field.arrayField[0].field = 1;
    }
}
