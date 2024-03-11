libsl "1.0.0";
library simple;
typealias int = int32;
automaton A : int {
    var d1: int = 0X123;
    var d2: int = 0x123;
    var d3: int = 0B1111;
    var d4: int = 0b1010;
    var d5: int = 0757;
}