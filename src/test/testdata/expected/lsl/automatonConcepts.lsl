libsl "1.0.0";
library simple;
type BufferedImage is java.awt.image.BufferedImage for Image, Object {
    width: int;
    content: array<array<int>>;
    iterator(offset): any;
}
types {
    Int(int32);
    String(string);
}
automaton concept IterableAutomaton {
    var storage: any;
    proc _getNext(index: int): any;
    proc something(): any;
}
automaton Foo : Int implements IterableAutomaton, CollectionAutomaton {
    fun bar(value: any) {
        `IterableAutomaton(value)._getNext`(5);
    }
    fun foo(newValue: any) {
        val x: any;
        if (newValue has IterableAutomaton) {
            `IterableAutomaton(newValue).something`();
        }
    }
}