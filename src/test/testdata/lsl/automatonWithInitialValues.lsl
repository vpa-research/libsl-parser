libsl "1.0.0";
library simple;

typealias Int=int32;

automaton A : Int {
    fun f(): any {
        result = new EveryVarIsOptional(state = init);
        result = new EveryVarIsOptional(state = init, var1 = 2);
        result = new EveryVarIsOptional(state = init, var1 = 2, var2 = "foo");

        result = new OneVarIsOptional(state = init, var1 = 2, var2 = "foo", var3 = "foo");
        result = new OneVarIsOptional(state = init, var2 = "foo", var3 = "foo");

        result = new EveryVarIsOptional(state = init, var3 = "foo");
        result = new EveryVarIsOptional(state = init, var1 = 2, var3 = "foo");
        result = new EveryVarIsOptional(state = init, var2 = "foo", var1 = 2, var3 = "foo");
    }
}

automaton EveryVarIsOptional : Int {
    var var1: Int = 1;
    var var2: string = "str";

    initstate init;
}

automaton OneVarIsOptional : Int {
    var var1: Int = 1;
    var var2: string;
    var var3: string;

    initstate init;
}

automaton FewVarsAreOptional : Int {
    var var1: Int = 1;
    var var2: string = "str";
    var var3: string;

    initstate init;
}
