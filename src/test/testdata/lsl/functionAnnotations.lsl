libsl "1.0.0";
library simple;

typealias Int=int32;

annotation static();
annotation void();
annotation anno(
    x: Int,
    y: Int,
);
annotation Target();
annotation something();

automaton B: Int {
}

automaton A : Int {
    @static
    fun f(@anno(1, 12) @Target obj: B);

    @void
    fun g(@anno(554, 784) @something param: Int)
}
