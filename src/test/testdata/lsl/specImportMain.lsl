libsl "1.0.0";
library simple;

import "specImportSecondary";

automaton A : Int {
   var i: Int;

   fun f(param: Int) {
      action TEST_ACTION(1, "123", param);
      action TEST_ACTION_TWO();
   }
}