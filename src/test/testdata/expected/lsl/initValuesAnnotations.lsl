libsl "1.0.0";
library simple;
typealias Int = int32;
annotation Anno(
    x: Int,
    y: Int
);
automaton A : Int {
    fun f(@Anno(x = 1, y = 12) param: Int);
    fun g(@Anno(x = 1, y = 12) param: Int, value: Int);
}