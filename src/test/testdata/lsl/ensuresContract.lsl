libsl "1.0.0";
library simple;

typealias Int=int32;

type StructureType {
    field: Int;
}

automaton A : Int {
    fun foo(param: Int)
        assigns param;

    fun foo(param: StructureType)
        assigns namedAssigns: param.field;
}