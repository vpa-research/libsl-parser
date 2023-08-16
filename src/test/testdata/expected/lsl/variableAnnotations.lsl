libsl "1.0.0";
library simple;
types {
    Int(int32);
}
@Const()
var x: Int = 1;
annotation Const;
annotation Something;
automaton A (@Something() var y: Int) : Int {
    @Something()
    var i: Int;
    fun f(param: Int) {
        requires i == 0;
        i = 1;
    }
    fun g(a: Int) {
        i = a;
    }
    fun a(i: Int) {
        i = 0;
    }
}