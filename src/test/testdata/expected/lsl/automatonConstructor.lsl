libsl "1.0.0";
library simple;

types {
    Int(int32);
    String(string);
}

automaton A (var i: Int, var s: String) : Int {
    var i: Int;

    fun func() {
        i = new B(state = s1, v = (1 + 1) / 2);
    }
}

automaton B (var v: Int) : Int {
    state s1;
}
