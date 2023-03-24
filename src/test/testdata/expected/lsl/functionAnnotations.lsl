libsl "1.0.0";
library simple;
typealias Int = int32;
automaton B : Int {
}
automaton A : Int {
    @Static
    fun f(@anno @target obj: B);
    @Void
    fun g(@anno(1, "12") @something param: Int);
}