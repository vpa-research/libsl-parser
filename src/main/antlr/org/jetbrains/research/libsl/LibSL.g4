grammar LibSL;

/*
 * Entry rule
 * Specification starts with header block ('libsl', 'library' and other keywords), then
 * semantic types section and declarations (automata and extension functions)
 */
file
   :   header
       globalStatement*
       EOF
   ;

globalStatement
   :   importStatement
   |   includeStatement
   |   typesSection
   |   typealiasStatement
   |   typeDefBlock
   |   enumBlock
   |   declaration
   ;
/*
 * Header section
 * Includes 'libsl' keyword with LibSL version, 'library' keyword with name of the library, and any of these optionally:
 * 'version', 'language' and 'url'
 */
header:
   ('libsl' lslver=DoubleQuotedString ';')
   ('library' libraryName=Identifier)
   ('version' ver = DoubleQuotedString)?
   ('language' lang=DoubleQuotedString)?
   ('url' link=DoubleQuotedString)?
   ';';

importStatement
   :   'import' importString=QuotedString ';'
   ;

includeStatement
   :   'include' includeString=QuotedString ';'
   ;

/* typealias statement
 * syntax: typealias name = origintlType
 */
typealiasStatement
   :   'typealias' left=typeIdentifier '=' right=typeIdentifier ';'
   ;

/* type define block
 * syntax: type full.name { field1: Type; field2: Type; ... }
 */
typeDefBlock
   :   'type' name=typeIdentifier ('{' typeDefBlockStatement* '}')?
   ;

typeDefBlockStatement
   :   nameWithType ';'
   ;

/* enum block
 * syntax: enum Name { Variant1=0; Variant2=1; ... }
 */
enumBlock
   :   'enum' typeIdentifier '{' enumBlockStatement* '}'
   ;

enumBlockStatement
   :   Identifier '=' integerNumber ';'
   ;

/*
 * Semantic types section
 */
typesSection
   :   'types' '{' semanticType* '}'
   ;

semanticType
   :    simpleSemanticType
   |    blockType
   ;

/*
 * syntax: semanticTypeName (realTypeName);
 */
simpleSemanticType
   :   semanticName=typeIdentifier '(' realName=typeIdentifier ')' ';'
   ;

/*
 * syntax: semanticTypeName (realTypeName) {variant1: Int; variant2: Int; ...};
 */
blockType
   :   semanticName=Identifier '(' realName=typeIdentifier ')' '{' blockTypeStatement+ '}'
   ;

blockTypeStatement
   :    Identifier ':' expressionAtomic ';'
   ;

declaration
   :   automatonDecl
   |   functionDecl
   |   variableDeclaration
   ;

/*
 * syntax: automaton Name [(constructor vars)] : type { statement1; statement2; ... }
 */
automatonDecl
   :   'automaton' name=Identifier ('(' 'var' nameWithType (',' 'var' nameWithType)* ')')? ':' type=Identifier '{' automatonStatement* '}'
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
 * syntax: shift from -> to(function1; function2(optional arg types); ...)
 * syntax: shift (from1, from2, ...) -> to(function1; function2(optional arg types); ...)
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
 * syntax: var NAME [: type] = { new AutomatonName(args); atomic; nothing }
 */

variableDeclaration
   :   'var' nameWithType ';'
   |   'var' nameWithType '=' assignmentRight ';'
   ;

nameWithType
   :   name=Identifier ':' type=typeIdentifier
   ;

variableAssignment
   :   qualifiedAccess '=' assignmentRight ';'
   ;

assignmentRight
   :   expression
   |   'new' callAutomatonConstructorWithNamedArgs
   ;

callAutomatonConstructorWithNamedArgs
   :   name=Identifier '(' (namedArgs)? ')'
   ;

namedArgs
   :   argPair (',' argPair)*
   ;

argPair
   :   name='state' '=' expressionAtomic
   |   name=Identifier '=' expression
   ;

/*
 * syntax: fun name(@annotation arg1: type, arg2: type, ...) [: type] { statement1; statement2; ... }
 * In case of declaring extension-function, name must look like Automaton.functionName
 */
functionDecl
   :   'fun' name=periodSeparatedFullName '(' functionDeclArgList? ')' (':' functionType=Identifier)? (';' | functionPreamble '{' functionBody '}')
   ;

functionDeclArgList
   :   parameter (',' parameter)*
   ;

parameter
   :   annotation? name=Identifier ':' type=Identifier
   ;

/* todo Should we allow multiple annotation per one parameter?
 * syntax: @annotationName(args) todo: add args
 */
annotation
   :   '@' Identifier ('(' valuesAndIdentifiersList ')')?
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
   :   expression (',' expression)*
   ;

requiresContract
   :   'requires' (name=Identifier ':')? expression ';'
   ;

ensuresContract
   :   'ensures' (name=Identifier ':')? expression ';'
   ;

expression
   :   lbracket='(' expression rbracket=')'
   |   expression op=('*' | '/') expression
   |   expression op='%' expression
   |   expression op=('+' | '-') expression
   |   '-' expression
   |   '!' expression
   |   expression op=('=' | '!=' | '<=' | '<' | '>=' | '>') expression
   |   expression op=('&' | '|' | '^') expression
   |   qualifiedAccess apostrophe='\''
   |   expressionAtomic
   |   qualifiedAccess
   ;

expressionAtomic
   :   qualifiedAccess
   |   primitiveLiteral
   ;

primitiveLiteral
   :   integerNumber
   |   floatNumber
   |   DoubleQuotedString
   |   bool=('true' | 'false')
   ;

qualifiedAccess
   :   periodSeparatedFullName
   |   qualifiedAccess '[' expressionAtomic ']'
   |   simpleCall '.' qualifiedAccess
   ;

simpleCall
   :   Identifier '(' Identifier ')'
   ;

/*
 * syntax: one.two.three<T>
 */
typeIdentifier
   :   (asterisk='*')? name=periodSeparatedFullName ('<' generic=typeIdentifier '>')?
   ;

Identifier
   :   [a-zA-Z_$][a-zA-Z0-9_$]*
   ;

identifierList
   :   Identifier (',' Identifier)*
   ;

DoubleQuotedString
   :   '"' .*? '"'
   ;

QuotedString
   :   '\'' .*? '\''
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

