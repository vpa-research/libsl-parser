libsl "1.0.0";
library simple;
types {
    Int(int32);
}
automaton A : Int {
    fun f(x: Int, y: Int) {
        val res: Int;
        ++x;
        x++;
        --x;
        x--;
        x = (x && y);
        x = (x || y);
        y = !x;
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