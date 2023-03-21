libsl "1.0.0";
library simple;
typealias Int = int32;
automaton A : Int {
    fun f(@anno param: Int);
    fun g(@anno(1, "12") param: Int);
}