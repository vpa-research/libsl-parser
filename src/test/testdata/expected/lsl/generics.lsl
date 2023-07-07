libsl "1.0.0";
library simple;
type List<in Int, out Collection<Int>> {
}
type Collection<Int> {
}
types {
    Int(int32);
}
automaton Foo : Int {
    fun bar(img: Int): Int {
        var b: bool = arg0 is List<in Int, out Collection<Int>>;
        var x: Collection<Int> = arg0 as List<in Int, out Collection<Int>>;
        result = x;
    }
}