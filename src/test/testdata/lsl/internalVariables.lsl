libsl "1.0.0";
library simple;

types {
   Int(int);
}

automaton A {
   var b: Int = new B(state = s);

   fun foo() {
       b.i = 1;
   }
}

automaton B {
   state s;
   var i: Int;
}