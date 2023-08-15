libsl "1.0.0";
library contextResolution2;
import contextResolution1;
typealias TypeFrom2File = int32;
typealias TypeFrom1File = int32;
automaton AutomatonFrom2File : TypeFrom1File {
    fun foo(a: TypeFrom1File): TypeFrom1File {
        result = new AutomatonFrom1File(state = S1);
    }
    fun functionFrom2File();
    fun functionFrom2File();
}
automaton AutomatonFrom1File : TypeFrom2File {
    initstate S1;
    fun functionFrom1File();
}
automaton AutomatonFrom2File : TypeFrom1File {
    fun foo(a: TypeFrom1File): TypeFrom1File {
        result = new AutomatonFrom1File(state = S1);
    }
    fun functionFrom2File();
    fun functionFrom2File();
}