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

type BlackAndWhiteImage {  // указываем полное имя типа
    height: Int;
    width: Int;
    content: Array<Array<Boolean>>;
}

automaton Image : BlackAndWhiteImage {
   fun inversePixel(img: BlackAndWhiteImage, x: Int, y: Int)
   requires size: (x > 0) & (y > 0);
   ensures img[y][x] != img[y][x]';
   {
       img.content[y][x] = !img.content[y][x];
   }
}