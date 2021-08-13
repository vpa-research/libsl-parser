libsl "1.0.0";
library simple;

types {
   Int(int);
}

automaton A : Int {
   var i: Int;

   fun f(param: Int)
   requires i = 0;
   {
      i = 1;
   }
}

fun A.g(a: Int) {
    i = a;
}

fun A.a(i: Int) {
    i = 0; // i is a parameter
}