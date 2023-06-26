libsl "1.0.0";
library simple;
enum foo.vldf.Type {
    Variant1 = 0;
    Variant2 = 1;
}
typealias MyType = foo.vldf.Type;
type StructureType {
    var field: Int;
}
type BlackAndWhiteImage {
    var height: Int;
    var width: Int;
    var tpe: StructureType;
    var content: array<array<Boolean>>;
}
types {
    Int(int32);
    Type(Int) {
        variant1: 0;
        variant2: 1;
    }
}

automaton Image : BlackAndWhiteImage {
    fun inversePixel(img: BlackAndWhiteImage, x: Int, y: Int) {
        requires size: x > 0 & y > 0;
        ensures img.content[y][x] != (img.content[y][x])';

        img.content[y][x] = !img.content[y][x];
        img.tpe.field = 1;
    }
}
