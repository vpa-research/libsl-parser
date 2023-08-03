libsl "1.0.0";
library simple;

types {
    Int(int32);
    String(string);
    Object(java.lang.Object);
    Image(java.img.Image);
    Iterator(iterator);
    Something(Something);
    Collection(Collection);
}

automaton IterableAutomaton : Int {
   var storage: any;
   proc _getNext (index: Int): any;
   proc something(): any;
}

automaton Foo(): Int {
   fun foo(value: any) {
      val x: any;
      if (this.value has IterableAutomaton) {
         something();
      }
   }
}
