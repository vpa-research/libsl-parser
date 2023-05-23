libsl "1.0.0";
library simple;

types {
    Int(int32);
    String(string);
}

/* type BufferedImage is java.awt.image.BufferedImage for Image, Object {
   width: int;
   content: array<array<int>>;
   iterator (offset: int): Iterator;
}


fun Foo.bar(img: BufferedImage): Object {
   result = img.iterator(this.offset + 2);
}
*/

automaton Foo(): Int implements IterableAutomaton, CollectionAutomaton
{
   fun bar (value: any) {
      _getNext(5);
   }

   fun something();
}
