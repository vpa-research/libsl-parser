libsl "1.0.0";
library simple;
typealias Int = int32;
annotation static;
annotation void;
annotation anno(
    x: Int = 1,
    y: Int
);
annotation something;
automaton B : Int {
}
automaton A : Int {
    @static()
    fun f(@anno(1, 12) @target() obj: Int);
    @void()
    fun g(@anno(554, 784) @something() param: Int);
}