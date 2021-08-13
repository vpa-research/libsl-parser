libsl "1.0.0";
library simple;

types {
   Int(int);
}

automaton A : Int {
   fun f(@annotation param: Int);
}
