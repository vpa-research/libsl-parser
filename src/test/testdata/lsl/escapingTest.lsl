libsl "1.0.0";
library simple;

typealias Int = int32;

automaton A : Int {
    var i: Int;

    fun f(param: Int): Int {
        result = new `123`(state = Init);
    }

    fun `<test>`(): Int { }
}

automaton `123` : Int {
    initstate Init;
}