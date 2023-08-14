libsl "1.1.0";
library std version "11" language "Java" url "-";
typealias int = int32;
type Object is java.lang.Object for Object {
}
annotation public;
define action THROW_NEW(
    exceptionType: string,
    params: array<any>
): void;