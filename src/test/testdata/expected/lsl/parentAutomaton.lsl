libsl "1.0.0";
library simple;
typealias Int = int32;
automaton A : Int {
    fun useProc(): Int {
        result = new B(state = Initialized, parent = A);
    }
}
automaton B : Int {
}