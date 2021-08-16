libsl "1.0.0";
library simple;

types {
   Int(int); // simple type
   Type(int) { // enum-like type
      variant1: 0;
      variant2: 1;
   }
}

enum foo.vldf.Type { // enum type
   Variant1 = 0;
   Variant2 = 1;
}

typealias MyType = foo.vldf.Type;

type StructureType {
    field: Type;
}

type BlackAndWhiteImage {
    height: Int;
    width: Int;
    tpe: StructureType;
    content: array<array<Boolean>>;
}

automaton Image : BlackAndWhiteImage {
   fun inversePixel(img: BlackAndWhiteImage, x: Int, y: Int)
   requires size: (x > 0) & (y > 0);
   ensures img.content[y][x] != img.content[y][x]';
   {
       img.content[y][x] = !img.content[y][x];
       img.tpe.field = 1;
   }
}