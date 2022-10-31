libsl "1.0.0";
library simple version "1.0.0f" language "java" url "https://github.com/vldf/";

include file.to.include;

types {
    SimpleType(ru.vldf.Type);
    SimpleTypeWithGeneric(ru.vldf.Type<ru.vldf.Generic>);
    EnumLikeType(ru.vldf.Type) {
        VARIANT1: -1;
        VARIANT2: true;
        VARIANT3: "String";
    }
    String(java.lang.String);
    Int(int32);
}

automaton A : Int {
    state S1;
    state S2;

    shift S1->S2(func);

    var strVar: String;
    var intVar: Int = 1;
    var b: Int = new B(state = S1);

    fun func(arg1: SimpleType): SimpleTypeWithGeneric;
}

automaton B : Int {
    state S1, S3, S7;
    state S2;

    shift S1->S2(func(SimpleTypeWithGeneric));
    shift S2->S1(func(SimpleType));

    var v: Int;

    fun func(arg1: SimpleType): SimpleTypeWithGeneric;
    fun func(arg1: SimpleTypeWithGeneric): SimpleTypeWithGeneric;
}

fun A.extendedFunction() {
    strVar = "";
    b = 1;
}
