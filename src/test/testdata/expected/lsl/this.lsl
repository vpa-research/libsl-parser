libsl "1.0.0";
library simple;
types {
    Int(int32);
}
automaton B (val x: Int) : Int {
    proc smth(): Int {
        result = 1 + 1;
    }
    fun foo(arg: Int): Int {
        val y: Int = this.v;
        result = this.x + y;
    }
    fun anotherFoo(): Int {
        val temp: Int = this;
        result = smth();
    }
}