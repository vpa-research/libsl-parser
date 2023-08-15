libsl "1.1.0";
library std version "11" language "Java" url "-";
import javaCommon;
typealias int = int32;
type Object is java.lang.Object for Object {
}
type CharSequence is java.lang.CharSequence for Object {
    fun length(): int;
    fun charAt(index: int): char;
    fun toString(): string;
}
type String is java.lang.String for CharSequence, string {
}
annotation public;
define action THROW_NEW(
    exceptionType: string,
    params: list<any>
): void;