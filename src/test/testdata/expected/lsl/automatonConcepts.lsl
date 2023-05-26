libsl "1.0.0";
library simple;
@StructureKind("record")
@Parametrized("P extends java.img.PixelType")
type BufferedImage is java.awt.image.BufferedImage for Image, Object {
    width: int;
    content: array<array<int>>;
    iterator(offset): Iterator;
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
automaton concept IterableAutomaton {
    var storage: any;
    proc _getNext(index: int): any;
    proc something(): any;
}
automaton Foo : Int implements IterableAutomaton, CollectionAutomaton {
    fun bar(img: BufferedImage): Object {
        result = `img.iterator`(this.offset + 2);
    }
    fun foo(newValue: any) {
        val x: any;
        if (newValue has IterableAutomaton) {
            `IterableAutomaton(newValue).something`();
        }
    }
}