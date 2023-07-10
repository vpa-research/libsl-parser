libsl "1.0.0";
library simple;
type List<in Int, out Collection<Int>> {
}
type Collection<Int> {
}
type T<Int> {
}
types {
    Int(int32);
}
automaton Foo (val y: Int) : T<Int> {
    state S1;
    fun bar(img: Int): Int {
        var b: bool = arg0 is List<in Int, out Collection<Int>>;
        var x: Collection<Int> = arg0 as List<in Int, out Collection<Int>>;
        result = x;
    }
}
automaton A : T<Int> {
    fun smth(): T<Int> {
        result = new Foo<T<Int>>(state = S1, y = 1);
    }
}