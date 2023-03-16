libsl "1.0.0";
library simple;
types {
    int(int32);
}
automaton a : int {
    state S1;
    state S2;
    state S3;
    shift S1 -> S2(foo);
    shift S1 -> S2(fix);
    shift S1 -> S2(fix, foo);
    shift S1 -> S2(foo);
    shift S1 -> S2(foo);
    shift S1 -> S3(sum(int, int), min(int));
    shift S1 -> S3(sum(int, int));
    shift S1 -> S3(foo);
    shift S2 -> S3(foo);
    shift S1 -> S3(sum(int, int), min(int));
    shift S2 -> S3(sum(int, int), min(int));
    shift S1 -> S3(fix);
    shift S2 -> S3(fix);
    fun foo();
    fun sum(i: int, j: int);
    fun min(i: int);
    fun fix();
}