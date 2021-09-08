grammar LibSL;

/*
 * entry rule
 * specification starts with header block ('libsl', 'library' and other keywords), then
 * semantic types section and declarations (automata and extension functions)
 */
file
   :   header
       globalStatement*
       EOF
   ;

globalStatement
   :   ImportStatement
   |   IncludeStatement
   |   typesSection
   |   typealiasStatement
   |   typeDefBlock
   |   enumBlock
   |   topLevelDecl
   ;

ImportStatement
   :   'import' .*? ';'
   ;

IncludeStatement
   :   'include' .*? ';'
   ;

topLevelDecl
   :   automatonDecl
   |   functionDecl
   |   variableDecl
   ;

/*
 * header section
 * includes 'libsl' keyword with LibSL version, 'library' keyword with name of the library, and any of these optionally:
 * 'version', 'language' and 'url'
 */
header:
   ('libsl' lslver=DoubleQuotedString ';')
   ('library' libraryName=Identifier)
   ('version' ver = DoubleQuotedString)?
   ('language' lang=DoubleQuotedString)?
   ('url' link=DoubleQuotedString)?
   ';';

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

/* semantic types section
 * syntax types { semanticTypeDeclaration1; semanticTypeDeclaration2; ... }
 */
typesSection
   :   'types' '{' semanticTypeDecl* '}'
   ;

semanticTypeDecl
   :    simpleSemanticType
   |    blockType
   ;

/* simple semantic type
 * syntax: semanticTypeName (realTypeName);
 */
simpleSemanticType
   :   semanticName=typeIdentifier '(' realName=typeIdentifier ')' ';'
   ;

/* block semantic type
 * syntax: semanticTypeName (realTypeName) {variant1: Int; variant2: Int; ...};
 */
blockType
   :   semanticName=Identifier '(' realName=typeIdentifier ')' '{' blockTypeStatement+ '}'
   ;

blockTypeStatement
   :    Identifier ':' expressionAtomic ';'
   ;

/* automaton declaration
 * syntax: automaton Name [(constructor vars)] : type { statement1; statement2; ... }
 */
automatonDecl
   :   'automaton' name=periodSeparatedFullName ('(' 'var' nameWithType (',' 'var' nameWithType)* ')')? ':' type=Identifier '{' automatonStatement* '}'
   ;

automatonStatement
   :   automatonStateDecl
   |   automatonShiftDecl
   |   functionDecl
   |   variableDecl
   ;

/* state declaration
 * syntax: one of {initstate; state; finishstate} name;
 */
automatonStateDecl
   :   keyword=('initstate' | 'state' | 'finishstate') identifierList ';'
   ;

/* shift declaration
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

functionsListPart
   :   name=Identifier ('(' Identifier? (',' Identifier)* ')')?
   ;

/* variable declaration with optional initializers
 * syntax: var NAME [= { new AutomatonName(args); atomic }]
 */
variableDecl
   :   'var' nameWithType ';'
   |   'var' nameWithType '=' assignmentRight ';'
   ;

nameWithType
   :   name=Identifier ':' type=typeIdentifier
   ;

/*
 * syntax: one.two.three<T>
 */
typeIdentifier
   :   (asterisk='*')? name=periodSeparatedFullName ('<' generic=typeIdentifier '>')?
   ;

variableAssignment
   :   qualifiedAccess '=' assignmentRight ';'
   ;

assignmentRight
   :   expression
   |   'new' callAutomatonConstructorWithNamedArgs
   ;

callAutomatonConstructorWithNamedArgs
   :   name=periodSeparatedFullName '(' (namedArgs)? ')'
   ;

namedArgs
   :   argPair (',' argPair)*
   ;

argPair
   :   name='state' '=' expressionAtomic
   |   name=Identifier '=' expression
   ;

/*
 * syntax: fun name(@annotation arg1: type, arg2: type, ...) [: type] [preambule] { statement1; statement2; ... }
 * In case of declaring extension-function, name must look like Automaton.functionName
 */
functionDecl
   :   'fun' name=periodSeparatedFullName '(' functionDeclArgList? ')' (':' functionType=Identifier)?
       (';' | functionPreamble ('{' functionBody '}')?)
   ;

functionDeclArgList
   :   parameter (',' parameter)*
   ;

parameter
   :   annotation? name=Identifier ':' type=Identifier
   ;

/* annotation
 * syntax: @annotationName(args)
 */
annotation
   :   '@' Identifier ('(' valuesAndIdentifiersList ')')?
   ;

/*
 * declarations between function's header and body-block
 */
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

/* semantic action
 * syntax: action ActionName(args)
 */
action
   :  'action' Identifier '(' valuesAndIdentifiersList? ')' ';'
   ;

valuesAndIdentifiersList
   :   expression (',' expression)*
   ;

/* requires contract
 * syntax: requires [name:] condition
 */
requiresContract
   :   'requires' (name=Identifier ':')? expression ';'
   ;

/* ensures contract
 * syntax: ensures [name:] condition
 */
ensuresContract
   :   'ensures' (name=Identifier ':')? expression ';'
   ;

/*
 * expression
 */
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

Identifier
   :   [a-zA-Z_$][a-zA-Z0-9_$]*
   |   '`' .*? '`'
   ;

identifierList
   :   Identifier (',' Identifier)*
   ;

DoubleQuotedString
   :   '"' .*? '"'
   ;

periodSeparatedFullName
   :   Identifier
   |   Identifier ('.' Identifier)*
   |   '`' Identifier ('.' Identifier)* '`'
   ;

Digit: ('0'..'9');

UNARY_MINUS: '-';

INV: '!';

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

