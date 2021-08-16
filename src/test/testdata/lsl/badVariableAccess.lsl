libsl "1.0.0";
library simple;

typealias Int = int32;
typealias T = array<Int>;

automaton A : Int {
   fun f(param: T, bad: Int) {
      param[0] = 1;
      bad[0] = 1;
   }
}