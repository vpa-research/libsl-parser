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
    var i1: Int = 50;
    var i2: UInt = 50u;
    var i3: Byte = 50x;
    var i4: UByte = 50ux;
    var i5: Short = 50s;
    var i6: UShort = 50us;
    var i7: Long = 50L;
    var i8: Long = 50l;
    var i9: ULong = 50uL;
}