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
    shift S1 -> S2(foo);
    shift S1 -> S2(ddd, ddd);
    shift S1 -> S2(a);
    shift S1 -> S2(aaa);
    shift S1 -> S3(dd(int), ddd(int), sfgsf(int));
    shift S1 -> S3(sum(int, int));
    fun foo();
    fun sum(i: int, j: int);
}