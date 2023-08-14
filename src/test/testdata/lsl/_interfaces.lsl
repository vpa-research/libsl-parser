//#! pragma: non-synthesizable
libsl "1.1.0";

library std
    version "11"
    language "Java"
    url "-";

import javaCommon;


type CharSequence
    is java.lang.CharSequence
    for Object
{
    fun length(): int;

    fun charAt(index: int): char;

    fun toString(): string; // #problem
}

type String is java.lang.String for CharSequence, string {}

