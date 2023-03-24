libsl "1.0.0";
library simple;
typealias Int = int32;
annotation Throws(
    exceptionTypes: Int = 0
);
annotation Public;
annotation Something(
    variable1: Int = 2,
    variable2: Int = 5
);
automaton A : Int {
    fun f(@anno param: Int);
    fun g(@anno(1, "12") param: Int);
}