libsl "1.0.0";
library simple;

types {
    int(int32);
}

automaton a: int {

    state S1;
    state S2;
    state S3;

    shift S1 -> S2 by foo;
    shift S1 -> S2(foo);
    shift S1 -> S2 by [ddd, ddd,];
    shift S1 -> S2 by [a];
    shift S1 -> S2 by [aaa,];
    shift S1 -> S3 by [dd(int), ddd(int), sfgsf(int),];
    shift S1 -> S3 by sum(int, int);

    fun foo();
    fun sum(i: int, j: int);
}