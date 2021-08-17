libsl "1.0.0";
library simple;

types {
   Int(int);
}

automaton A : Int {
   var i: Int;

   fun f(param: Int) {
      action TEST_ACTION(1, "123", param, 1+123);
      action TEST_ACTION_TWO();
   }
}