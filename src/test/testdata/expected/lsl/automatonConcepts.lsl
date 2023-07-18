libsl "1.0.0";
library simple;
@StructureKind("record")
@Parametrized("P extends java.img.PixelType")
type BufferedImage is java.awt.image.BufferedImage for Image, Object {
    var width: int;
    var content: array<array<int, string>>;
    static fun iterator(offset: int): Iterator;
}
type Collection {
}
types {
    @implements
    Int(int32);
    String(string);
    Object(java.lang.Object);
    Image(java.img.Image);
    Iterator(iterator);
}
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
        var x: Collection = arg0 as List;
        result = iterator(this.offset + 2);
    }
    fun foo(newValue: any) {
        val x: any;
        if (this.newValue has IterableAutomaton) 
            something();
    }
}