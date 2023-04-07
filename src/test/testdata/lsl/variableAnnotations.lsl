libsl "1.0.0";
library simple;

types {
    Int(int32);
}

annotation Const;
annotation Something;

@Const
var x: Int = 1;

automaton A (
    var y: Int;
): Int{

    @Something
    var i: Int;

    fun f(param: Int) {
        requires i == 0;
        i = 1;
    }
}

fun A.g(a: Int) {
    i = a;
}

fun A.a(i: Int) {
    i = 0; // i is a parameter
}
