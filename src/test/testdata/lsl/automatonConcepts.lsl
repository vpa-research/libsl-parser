libsl "1.0.0";
library simple;

types {
    @implements
    Int(int32);
    String(string);
    Object(java.lang.Object);
    Image(java.img.Image);
    Iterator(iterator);
}

@StructureKind("record")
@Parametrized("P extends java.img.PixelType")
type BufferedImage is java.awt.image.BufferedImage for Image, Object {
   var width: int;
   var content: array<array<int>>;
   fun iterator(offset: int): Iterator;
}

annotation StructureKind(str: string);
annotation Parametrized(str: string);
annotation implements();

automaton concept IterableAutomaton {
   var storage: any;
   proc _getNext (index: int): any;
   proc something(): any;
}

automaton Foo(): Int implements IterableAutomaton, CollectionAutomaton
{
   fun bar (img: BufferedImage): Object {
      result = img.iterator(this.offset + 2);
   }

   fun foo(newValue: any) {
      val x: any;
      if (this.newValue has IterableAutomaton) {
         IterableAutomaton(newValue).something();
      }
   }
}
