libsl "1.0.0";
library contextResolution2;

import contextResolution1;

typealias TypeFrom2File=int32;

automaton AutomatonFrom1File : Int {
    fun foo(a: TypeFrom1File): TypeFrom2File {
        result = new AutomatonFrom1File(state = S1);
    }
}

fun AutomatonFrom1File.functionFrom1File()
