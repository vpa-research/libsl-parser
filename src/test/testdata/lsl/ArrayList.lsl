///#! pragma: non-synthesizable
libsl "1.1.0";

library std
    version "11"
    language "Java"
    url "-";

import javaCommon;
import _interfaces;


// automata

automaton ArrayListAutomaton
(
)
: int
{

    proc _checkValidIndex (): void
    {
        val message: String = "Index ";
        action THROW_NEW("java.lang.IndexOutOfBoundsException", [message]);
    }
}