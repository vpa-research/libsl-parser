libsl "1.0.0";
library simple;

types {
    Int(int32);
}

automaton A : Int {

    fun f(x: Int, y: Int) {

        var res: Int;

        x = x && y;
        x = x || y;
        y = !x;
        y = ~x;
        y = +x;
        y = -x;

        res = x >> y;
        x >> y;
        res = x << y;
        x << y;
        res = x >>> y;
        x >>> y;
        x += y;
        x -= y;
        x *= y;
        x /= y;
        x %= y;
        x &= y;
        x |= y;
        x ^= y;
        x >>= y;
        x <<= y;
    }
}
