libsl "1.0.0";
library simple;
typealias Int = int32;
typealias Char = char;

automaton A : Int {
    var ch1: Char = '\u0041';
    var ch2: Char = 'a';
    var ch3: Char = '\u72D0';
    var ch4: Char = '\52';
    var ch5: Char = '\123';
    var ch6: Char = '狐';
    var ch7: Char = '\n';
    var ch8: Char = '\r';
    var ch9: Char = '\t';
    var ch10: Char = '\b';
    var ch11: Char = '\'';
    var ch12: Char = '\"';
    var ch13: Char = '\\';
    var ch14: Char = '\0';
    var ch15: Char = '\f';
    var ch16: Char = '\001';
    var ch17: Char = 'ሴ';
    var ch18: Char = '\u1234';
}