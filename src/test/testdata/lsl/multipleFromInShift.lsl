libsl "1.0.0";
library simple;

types {
   Int(int);
}

automaton A {
   var i: Int;

   state s1, s2, s3;

   shift (s1, s2) -> s3 (f);

   fun f(param: Int);
}