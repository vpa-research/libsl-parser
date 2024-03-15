libsl "1.0.0";
library simple;
typealias Int = int32;
typealias UInt = unsigned32;
typealias Byte = int8;
typealias UByte = unsigned8;
typealias Short = int16;
typealias UShort = unsigned16;
typealias Long = int64;
typealias ULong = unsigned64;
automaton A : Int {
    var d1: Int = 0X123;
    var d2: Int = 0x123;
    var d3: Int = 0B1111;
    var d4: Int = 0b1010;
    var d5: Int = 0757;

    var d6: Long = 0X123l;
    var d7: Long = 0x123L;
    var d8: Long = 0B1111l;
    var d9: Long = 0b1010L;
    var d10: Long = 0757l;
    var d11: Long = 0757L;

    var plainZero: Int = 0;

    var d12: UInt = 0xFFu;
    var d13: ULong = 0x17uL;
    var d14: UShort = 0x17us;
    var d15: UByte = 0x17ub;
    var d16: Byte = 0x17b;
    var d17: Short = 0x17s;

    var d18: UInt = 0b111111u;
    var d19: ULong = 0b111111uL;
    var d20: UShort = 0b111111us;
    var d21: UByte = 0b111111ub;
    var d22: Byte = 0b111111b;
    var d23: Short = 0b111111s;

    var d24: UInt = 0133u;
    var d25: ULong = 0133uL;
    var d26: UShort = 0133us;
    var d27: UByte = 0133ub;
    var d28: Byte = 0133b;
    var d29: Short = 0133s;

    var d30: UInt = 4294967295u;
    var d31: ULong = 18446744073709551615uL;
    var d32: UShort = 65535us;
    var d33: UByte = 255ub;
    var d34: Byte = -128b;
    var d35: Byte = 127b;
    var d36: Short = -32768s;
    var d37: Short = 32767s;
}