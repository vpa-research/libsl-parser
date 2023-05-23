libsl "1.0.0";
library simple;
types {
    Int(int32);
    String(string);
}
automaton Foo : Int implements IterableAutomaton, CollectionAutomaton {
    fun bar(value: any) {
        _getNext(5);
    }
    fun something();
}