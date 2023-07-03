libsl "1.0.0";
library simple;
@StructureKind("record")
@Parametrized("P extends java.img.PixelType")
type BufferedImage <A, B, C> is java.awt.image.BufferedImage for Image, Object 
    where
    A: Something,
    B: SomethingElse,
    C: Int
{
    var width: Int;
    var content: array<Something<Int>, String, Object>;
    static fun iterator(offset: int): Iterator;
}
types {
    @implements
    Int(int32);
    String(string);
    Object(java.lang.Object);
    Image(java.img.Image);
    Iterator(iterator);
    Something(Something);
    List(List);
    Collection(Collection);
}
var width: Int;
var content: array<Something<Int>, String, Object>;
result
var width: Int;
var content: array<Something<Int>, String, Object>;
annotation StructureKind(
    str: string
);
annotation Parametrized(
    str: string
);
annotation implements;
automaton concept IterableAutomaton : Int {
    var storage: any;
    proc _getNext(index: int): any;
    proc something(): any;
}
automaton Foo : Int implements IterableAutomaton, CollectionAutomaton {
    fun bar(img: BufferedImage): Object {
        var b: bool = arg0 is List;
        var x: `<UNRESOLVED_TYPE>` = arg0 as List;
        result = `img.iterator`(this.offset + 2);
    }
    fun foo(newValue: any) {
        val x: any;
        if (this.newValue has IterableAutomaton) {
            `IterableAutomaton(newValue).something`();
        }
    }
}