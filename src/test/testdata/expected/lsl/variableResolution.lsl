libsl "1.0.0";
library simple;

types {
    Int(int32);
}
automaton A : Int {
    var i: Int;
    
    fun f(param: Int)
    requires (i = 0);
    {
        i = 1;
    }
    
    fun g(a: Int) {
        i = a;
    }
    
    fun a(i: Int) {
        i = 0;
    }
}
