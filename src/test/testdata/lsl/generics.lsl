libsl "1.0.0";
library simple;

types {
    Int(int32);
}

type List<in Int, out Collection<Int>> {
}

type Collection<Int> {
}

automaton Foo(): Int
{
   fun bar (img: Int): Int {
      var b: bool = arg0 is List<Int, Collection<Int>>;
      var x: Collection<Int> = arg0 as List<Int, Collection<Int>>;
      result = x;
   }
}
