libsl "1.0.0";
library simple;
typealias Int = int32;
typealias Char = char;

automaton A : Int {
    var ch1: Char = '\u0041';
    var ch2: Char = 'a';
    var ch3: Char = 52;
    var ch4: Char = '\52';
    var ch5: Char = '\5';
    var ch6: Char = '\123';
}