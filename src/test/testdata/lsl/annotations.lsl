libsl "1.0.0";
library simple;

typealias Int=int32;

annotation Something;
annotation Public;
annotation Anno(
    x: Int,
    y: Int,
);

automaton A : Int {

    @Public
    fun f(@Anno(1, 12) param: Int);

    @Public
    fun g(@Anno(1, 12) param: Int, @Something value: Int);
}
