lexer grammar LibSLLexer;

@header {package org.jetbrains.research.libsl;}

SEMICOLON : ';' ;

ASSIGN_OP : '=' ;

EQ : '==' ;

L_BRACE : '{' ;

R_BRACE : '}' ;

L_BRACKET : '(' ;

R_BRACKET : ')' ;

L_SQUARE_BRACKET : '[' ;

R_SQUARE_BRACKET : ']' ;

DOT : '.' ;

COLON : ':' ;

COMMA : ',' ;

MINUS_ARROW : '->' ;

L_ARROW : '<' ;

R_ARROW : '>' ;

ASTERISK : '*' ;

SLASH : '/' ;

PERCENT : '%' ;

PLUS : '+' ;

MINUS : '-' ;

INCREMENT : '++' ;

DECREMENT : '--' ;

PLUS_EQ : '+=' ;

MINUS_EQ : '-=' ;

ASTERISK_EQ : '*=' ;

SLASH_EQ : '/=' ;

PERCENT_EQ : '%=' ;

EXCLAMATION : '!' ;

EXCLAMATION_EQ : '!=' ;

L_ARROW_EQ : '<=' ;

R_ARROW_EQ : '>=' ;

AMPERSAND : '&' ;

DOUBLE_AMPERSAND : '&&' ;

BIT_OR : '|' ;

LOGIC_OR : '||' ;

XOR : '^' ;

TILDE : '~' ;

AMPERSAND_EQ : '&=' ;

OR_EQ : '|=' ;

XOR_EQ : '^=' ;

R_SHIFT_EQ: '>>=' ;

L_SHIFT_EQ: '<<=' ;

APOSTROPHE : '\'' ;

BACK_QOUTE : '`' ;

ImportStatement
   :   IMPORT .*? ';'
   ;

IncludeStatement
   :   INCLUDE .*? ';'
   ;

IMPORT
   :   'import'
   ;

INCLUDE
   :   'include'
   ;

LIBSL
   :   'libsl'
   ;

LIBRARY
   :   'library'
   ;

VERSION
   :   'version'
   ;

LANGUAGE
   :   'language'
   ;

URL
   :   'url'
   ;

TYPEALIAS
   :   'typealias'
   ;

TYPE
   :   'type'
   ;

TYPES
   :   'types'
   ;

ENUM
   :   'enum'
   ;

ANNOTATION
   :   'annotation'
   ;

AUTOMATON
   :   'automaton'
   ;

CONCEPT
   :   'concept'
   ;

VAR
   :   'var'
   ;

VAL
   :   'val'
   ;

INITSTATE
   :   'initstate'
   ;

STATE
   :   'state'
   ;

FINISHSTATE
   :   'finishstate'
   ;

SHIFT
   :   'shift'
   ;

NEW
   :   'new'
   ;

FUN
   :   'fun'
   ;

CONSTRUCTOR
   :   'constructor'
   ;

DESTRUCTOR
   :   'destructor'
   ;

PROC
   :   'proc'
   ;

AT
   :   '@'
   ;

ACTION
   :   'action'
   ;

REQUIRES
   :   'requires'
   ;

ENSURES
   :   'ensures'
   ;

ASSIGNS
   :   'assigns'
   ;

TRUE
   :   'true'
   ;

FALSE
   :   'false'
   ;

DEFINE
   :   'define'
   ;

IF
   :   'if'
   ;

ELSE
   :   'else'
   ;

BY
   :   'by'
   ;

IS
   :   'is'
   ;

AS
   :   'as'
   ;

NULL
   :   'null'
   ;

Identifier
   :   [a-zA-Z_$][a-zA-Z0-9_$]*
   |   '`' .*? '`'
   ;

fragment ESCAPED_QUOTE
   : '\\"'
   ;

DoubleQuotedString
   :   '"' ( ESCAPED_QUOTE | ~('\n'|'\r') )*? '"'
   ;

CHARACTER
   :   '\'' SingleCharacter '\''
   |   '\'' EscapeSequence '\''
   ;

fragment
SingleCharacter
    :   ~['\\\r\n]
    ;

fragment
EscapeSequence
    :   '\\u' Hex Hex Hex Hex Hex Hex Hex Hex
    ;

Digit: ('0'..'9');

Hex: Digit | ('A'..'F');

fragment
NEWLINE
  : '\r' '\n' | '\n' | '\r'
  ;

/*
 *  Whitespace and comments
 */
WS
   :   [ \t]+ -> channel(HIDDEN)
   ;

BR
   :   [\r\n\u000C]+ -> channel(HIDDEN)
   ;

COMMENT
   :   '/*' .*? '*/' -> channel(HIDDEN)
   ;

LINE_COMMENT
   :   ('//' ~[\r\n]*) -> channel(HIDDEN)
   ;
