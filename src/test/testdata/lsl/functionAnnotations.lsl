libsl "1.0.0";
library simple;

typealias Int=int32;

annotation Static;
annotation Void;
annotation Anno(
    x: Int,
    y: Int,
);
annotation Target;
annotation Something;

automaton B: Int {
}

automaton A : Int {

    @Static
    fun f(@Anno(1, 12) @Target obj: B);

    @Void
    fun g(@Anno(554, 784) @Something param: Int)
}
