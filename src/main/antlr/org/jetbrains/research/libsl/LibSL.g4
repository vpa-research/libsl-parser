grammar LibSL;

/*
 * Entry rule
 * Specification starts with header block ('libsl', 'library' and other keywords), then
 * semantic types section and declarations (automata and extension functions)
 */
file
   :   header imports includes typesSection declarations EOF
   ;

/*
 * Header section
 * Includes 'libsl' keyword with LibSL version, 'library' keyword with name of the library, and any of these optionally:
 * 'version', 'language' and 'url'
 */
header:
   ('libsl' lslver=QuotedString ';')
   ('library' libraryName=Identifier)
   ('version' ver = QuotedString)?
   ('language' lang=QuotedString)?
   ('url' link=QuotedString)?
   ';';

imports
   :   oneImport*
   ;

oneImport
   :   'import' importString=QuotedString ';'
   ;

includes
   :   include*
   ;

include
   :   'include' includeString=QuotedString ';'
   ;

/*
 * Semantic types section
 */
typesSection
   :   'types' '{' typesSectionBody '}'
   ;

typesSectionBody
   :    semanticType*
   ;

semanticType
   :    simpleSemanticType  // todo: are we need change syntax?
   |    enumLikeSemanticType
   ;

/*
 * syntax: semanticTypeName (realTypeName);
 */
simpleSemanticType
   :   semanticTypeName=Identifier '(' realTypeName ')' ';'
   ;

/*
 * syntax: semanticTypeName (realTypeName) {variant1: Int; variant2: Int; ...};
 */
enumLikeSemanticType
   :   semanticTypeName=Identifier '(' realTypeName ')' '{' enumLikeSemanticTypeBody '}'
   ;

enumLikeSemanticTypeBody
   :   enumLikeSemanticTypeBodyStatement+
   ;

enumLikeSemanticTypeBodyStatement
   :    Identifier ':' expressionAtomic ';'
   ;

declarations
   :  declaration*
   ;

declaration
   :   automatonDecl
   |   functionDecl
   ;

/*
 * syntax: automaton Name { statement1; statement2; ... }
 */
automatonDecl
   :   'automaton' name=Identifier ('(' 'var' nameWithType (',' 'var' nameWithType)* ')')? '{' automatonStatement* '}'
   ;

automatonStatement
   :   automatonStateDecl
   |   automatonShiftDecl
   |   functionDecl
   |   variableDeclaration
   ;

/*
 * syntax: on of {initstate; state; finishstate} name;
 */
automatonStateDecl
   :   keyword=('initstate' | 'state' | 'finishstate') identifierList ';'
   ;

/*
 * syntax: shift from->to(function1; function2(optional arg types); ...)
 */
automatonShiftDecl
   :   'shift' from=Identifier '->' to=Identifier '(' functionsList? ')' ';'
   |   'shift' from='(' identifierList ')' '->' to=Identifier '(' functionsList? ')' ';'
   ;

functionsList
   :   functionsListPart (',' functionsListPart)*
   ;

functionsListPart // todo: check, is it ok?
   :   name=Identifier ('(' (Identifier)* ')')?
   ;

/*
 * syntax: var NAME [: type] = { new AutomatonName(args); primitive_type; nothing }
 */

variableDeclaration
   :   'var' nameWithType ';'
   |   'var' nameWithType '=' assignmentRight ';'
   ;

nameWithType
   :   name=Identifier ':' type=Identifier
   ;

variableAssignment
   :   qualifiedAccess '=' assignmentRight ';'
   ;

assignmentRight
   :   expressionAtomic
   |   'new' callAutomatonWithNamedArgs
   ;

callAutomatonWithNamedArgs
   :   name=Identifier '(' (namedArgs)? ')'
   ;

namedArgs
   :   argPair (',' argPair)*
   ;

argPair
   :   name='state' '=' value=expressionAtomic
   |   name=Identifier '=' value=expressionAtomic
   ;

/*
 * syntax: fun name(arg1: type, arg2: type, ...) [: type] { statement1; statement2; ... }
 * In case of declaring extension-function, name must look like Automaton.functionName
 */
functionDecl
   :   'fun' name=periodSeparatedFullName '(' functionDeclArgList? ')' (':' functionType=Identifier)? (';' | functionPreamble '{' functionBody '}')
   ;

functionDeclArgList
   :   parameter (',' parameter)*
   ;

parameter
   :   name=Identifier ':' type=Identifier
   ;

functionPreamble
   :   preamblePart*
   ;

preamblePart
   :   requiresContract
   |   ensuresContract
   ;

functionBody
   :   functionBodyStatements*
   ;

functionBodyStatements
   :   variableAssignment
   |   action
   ;

action
   :  'action' Identifier '(' valuesAndIdentifiersList? ')' ';'
   ;

valuesAndIdentifiersList
   :   expressionAtomic (',' expressionAtomic)*
   ;

requiresContract
   :   'requires' (name=Identifier ':')? contractExpression ';'
   ;

ensuresContract
   :   'ensures' (name=Identifier ':')? contractExpression ';'
   ;

contractExpression
   :   '(' contractExpression ')'
   |   contractExpression op=('*' | '/') contractExpression
   |   contractExpression op='%' contractExpression
   |   contractExpression op=('+' | '-') contractExpression
   |   '-' contractExpression
   |   '!' contractExpression
   |   contractExpression op=('==' | '!=' | '<=' | '<' | '>=' | '>') contractExpression
   |   contractExpression op=('&' | '|' | '^') contractExpression
   |   qualifiedAccess apostrophe='\''
   |   expressionAtomic
   |   qualifiedAccess
   ;

expressionAtomic
   :   integerNumber
   |   floatNumber
   |   qualifiedAccess
   |   QuotedString
   ;

qualifiedAccess
   :   periodSeparatedFullName
   |   qualifiedAccess '[' integerNumber ']'
   ;

/*
 * syntax: one.two.three<T>
 */
realTypeName
   :   periodSeparatedFullName ('<' generic=periodSeparatedFullName '>')?
   ;

Identifier
   :   [a-zA-Z_$][a-zA-Z0-9_$]*
   ;

identifierList
   :   Identifier (',' Identifier)*
   ;

QuotedString
   :   '"' .*? '"'
   ;

periodSeparatedFullName
   :   Identifier
   |   Identifier ('.' Identifier)*
   ;


Digit
   :   ('0'..'9')
   ;

UNARY_MINUS
   :   '-'
   ;

INV
   :   '!'
   ;

integerNumber
   :   UNARY_MINUS? Digit+
   |   Digit
   ;

floatNumber
   :  UNARY_MINUS? Digit+ '.' Digit+
   ;

fragment
NEWLINE
  : '\r' '\n' | '\n' | '\r'
  ;

/*
 *  Whitespace and comments
 */
WS
   :   [ \t]+ -> skip
   ;

BR
   :   [\r\n\u000C]+ -> skip
   ;

COMMENT
   :   '/*' .*? '*/' -> skip
   ;

LINE_COMMENT
   :   (' //' ~[\r\n]* | '// ' ~[\r\n]*) -> skip
   ;

