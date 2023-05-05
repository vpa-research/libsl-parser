libsl "1.0.0";
library contextResolution1;

import "contextResolution2";

typealias TypeFrom1File = int32;

automaton AutomatonFrom1File : TypeFrom2File {
    initstate S1;
    fun functionFrom1File();
}
