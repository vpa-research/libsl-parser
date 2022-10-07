libsl "1.0.0";
library simple;

types {
    Int(int32);
}

automaton A : Int {
   var i: Int;

   fun f(a: Int) {
      A(a).i = 0;
   }
}