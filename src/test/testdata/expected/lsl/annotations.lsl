libsl "1.0.0";
library simple;
@Something
typealias Int = int32;
annotation Something;
annotation Public;
annotation Anno(
    x: Int,
    y: Int
);
automaton A : Int {
    @Public
    fun f(@Anno(x = 1, y = 12) param: Int);
    @Public
    fun g(@Anno(x = 1, y = 12) param: Int, @Something value: Int);
}
