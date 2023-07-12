libsl "1.0.0";
library simple;

types {
    @implements
    Int(int32);
    String(string);
    Object(java.lang.Object);
    Image(java.img.Image);
    Iterator(iterator);
    Something(Something);
    Collection(Collection);
}

type List<Int> {
}

type SomethingElse<B> {
}

type Collection<Int> {
}

@StructureKind("record")
@Parametrized("P extends java.img.PixelType")
type BufferedImage<A, B, C>
    is java.awt.image.BufferedImage
    for Image, Object
    where
        A: Something,
        B: SomethingElse<B>,
        C: Int
{
   var width: Int;
   var content: array<Something<Int>, String, Object>;
   static fun iterator(offset: Int): Iterator;
}

annotation StructureKind(str: string);
annotation Parametrized(str: string);
annotation implements;

automaton concept IterableAutomaton : Int {
   var storage: any;
   proc _getNext (index: Int): any;
   proc something(): any;
}

automaton Foo(): Int implements IterableAutomaton, CollectionAutomaton
{
   fun bar (img: BufferedImage<A, B, C>): Object {
      var b: bool = arg0 is List<Int>;
      var x: Collection<Int> = arg0 as List<Int>;
      result = img.iterator(this.offset + 2);
   }

   fun foo(newValue: any) {
      val x: any;
      if (this.newValue has IterableAutomaton) {
         IterableAutomaton(newValue).something();
      }
   }
}
