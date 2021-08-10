libsl "1.0.0";
library simple;

types {
   Int(int);
}

automaton A : Int {
   var i: Int;

   fun f(param: Int)
   requires test1: param >= i;
   ensures param' < param;
   {

   }
}