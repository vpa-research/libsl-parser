libsl "1.0.0";
library simple;

@Something
typealias Int=int32;

annotation Something();
annotation static();
annotation Anno(
    x: Int,
    y: Int,
);

automaton A : Int {

    @static
    fun f(@Anno(x = 1, y = 12) param: Int);

    @static
    fun g(@Anno(x = 1, y = 12) param: Int, @Something value: Int);
}
