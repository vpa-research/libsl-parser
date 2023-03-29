libsl "1.0.0";
library simple;
types {
    int(int32);
}
automaton a : int {
    state S1;
    state S2;
    state S3;
    shift S1 -> S2 by [foo];
    shift S1 -> S2 by [fix, foo];
    shift S1 -> S2 by [foo];
    shift S1 -> S2 by [foo];
    shift S1 -> S3 by [sum(int, int), min(int)];
    shift S1 -> S3 by [sum(int, int)];
    shift S1 -> S3 by [foo];
    shift S2 -> S3 by [foo];
    shift S1 -> S3 by [sum(int, int), min(int)];
    shift S2 -> S3 by [sum(int, int), min(int)];
    fun foo();
    fun sum(i: int, j: int);
    fun min(i: int);
    fun fix();
}