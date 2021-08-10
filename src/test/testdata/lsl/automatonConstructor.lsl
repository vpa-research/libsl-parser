libsl "1.0.0";
library simple;

types {
   Int(int);
   String(string);
}

automaton A(
   var i: Int,
   var s: String
) {
   var i: Int;

   fun func() {
      i = new B(state = s1, v = 0);
   }
}

automaton B(
   var v: Int
) {
   state s1;
}