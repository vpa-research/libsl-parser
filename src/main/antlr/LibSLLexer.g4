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

COMPOUND_ADD : '+=' ;

COMPOUND_SUB : '-=' ;

COMPOUND_MULT : '*=' ;

COMPOUND_DIV : '/=' ;

COMPOUND_MOD : '%=' ;

EXCLAMATION : '!' ;

NOT_EQ : '!=' ;

LESS_EQ : '<=' ;

GREAT_EQ : '>=' ;

BIT_AND : '&' ;

LOGIC_AND : '&&' ;

BIT_OR : '|' ;

LOGIC_OR : '||' ;

XOR : '^' ;

BIT_COMPLEMENT : '~' ;

COMPOUND_AND : '&=' ;

COMPOUND_OR : '|=' ;

COMPOUND_XOR : '^=' ;

COMPOUND_SHIFT_R: '>>=' ;

COMPOUND_SHIFT_L: '<<=' ;

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

Identifier
   :   [a-zA-Z_$][a-zA-Z0-9_$]*
   |   '`' .*? '`'
   ;

DoubleQuotedString
   :   '"' .*? '"'
   ;

Digit: ('0'..'9');

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
   :   (' //' ~[\r\n]* | '// ' ~[\r\n]*) -> channel(HIDDEN)
   ;
