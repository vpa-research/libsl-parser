libsl "1.0.0";
library simple;
types {
    Int(int32);
}
automaton A : Int {
    proc smth(): Int {
        result = (1 + 1);
    }
}
automaton B (val x: Int) : Int {
    fun foo(arg: Int): Int {
        assigns this.parent;;
        result = (x + arg);
    }
    fun anotherFoo() {
        smth();
    }
}