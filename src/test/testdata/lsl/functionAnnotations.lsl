libsl "1.0.0";
library simple;

typealias Int=int32;

automaton B: Int {
}

automaton A : Int {

    @Static
    fun f(@annotation @target obj: B);

    @Void
    fun g(@anno(1, "12") @something param: Int)
}