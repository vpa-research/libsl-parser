libsl "1.0.0";
library simple;
typealias Int = int32;

automaton A : Int {

    fun use(): Int {
        result = new B(state = Initialized, parent = A);
    }

}

automaton B : Int {
    fun something();
}