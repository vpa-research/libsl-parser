libsl "1.0.0";
library simple;

types {
    Int(int32);
}
automaton A : Int {
    var b: Int = new B(state = s);
    
    fun foo() {
        b = 1;
    }
}
automaton B : Int {
    state s;
    var i: Int;
    
}
