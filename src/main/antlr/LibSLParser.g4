parser grammar LibSLParser;

@header {package org.jetbrains.research.libsl;}

options { tokenVocab = LibSLLexer; }

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
   (LIBSL lslver=DoubleQuotedString SEMICOLON)
   (LIBRARY libraryName=Identifier)
   (VERSION ver = DoubleQuotedString)?
   (LANGUAGE lang=DoubleQuotedString)?
   (URL link=DoubleQuotedString)?
   SEMICOLON;

/* typealias statement
 * syntax: typealias name = origintlType
 */
typealiasStatement
   :   TYPEALIAS left=typeIdentifier EQ right=typeIdentifier SEMICOLON
   ;

/* type define block
 * syntax: type full.name { field1: Type; field2: Type; ... }
 */
typeDefBlock
   :   TYPE name=periodSeparatedFullName (L_BRACE typeDefBlockStatement* R_BRACE)?
   ;

typeDefBlockStatement
   :   nameWithType SEMICOLON
   ;

/* enum block
 * syntax: enum Name { Variant1=0; Variant2=1; ... }
 */
enumBlock
   :   ENUM typeIdentifier L_BRACE enumBlockStatement* R_BRACE
   ;

enumBlockStatement
   :   Identifier EQ integerNumber SEMICOLON
   ;

/* semantic types section
 * syntax types { semanticTypeDeclaration1; semanticTypeDeclaration2; ... }
 */
typesSection
   :   TYPES L_BRACE semanticTypeDecl* R_BRACE
   ;

semanticTypeDecl
   :    simpleSemanticType
   |    enumSemanticType
   ;

/* simple semantic type
 * syntax: semanticTypeName (realTypeName);
 */
simpleSemanticType
   :   semanticName=typeIdentifier L_BRACKET realName=typeIdentifier R_BRACKET SEMICOLON
   ;

/* block semantic type
 * syntax: semanticTypeName (realTypeName) {variant1: 0; variant2: 1; ...};
 */
enumSemanticType
   :   semanticName=Identifier L_BRACKET realName=typeIdentifier R_BRACKET L_BRACE enumSemanticTypeEntry+ R_BRACE
   ;

enumSemanticTypeEntry
   :   Identifier COLON expressionAtomic SEMICOLON
   ;

annotationDecl
   :   ANNOTATION name=Identifier L_BRACKET (VAR nameWithType (EQ assignmentRight)?)? R_BRACKET SEMICOLON
   ;

/* automaton declaration
 * syntax: @Annotation
 *         automaton Name [(constructor vars)] : type { statement1; statement2; ... }
 */
automatonDecl
   :   automatonAnnotations*?
   AUTOMATON name=periodSeparatedFullName (L_BRACKET VAR nameWithType (COMMA VAR nameWithType)* R_BRACKET)? COLON type=periodSeparatedFullName L_BRACE automatonStatement* R_BRACE
   ;

/* automaton annotation
 * syntax: @annotationName(args)
 */
automatonAnnotations
   :  AT Identifier (L_BRACKET argsList R_BRACKET)?
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
   :   keyword=(INITSTATE | STATE | FINISHSTATE) identifierList SEMICOLON
   ;

/* shift declaration
 * syntax: shift from -> to(function1; function2(optional arg types); ...)
 * syntax: shift (from1, from2, ...) -> to(function1; function2(optional arg types); ...)
 */
automatonShiftDecl
   :   SHIFT from=Identifier MINUS_ARROW to=Identifier L_BRACKET functionsList? R_BRACKET SEMICOLON
   |   SHIFT from=L_BRACKET identifierList R_BRACKET MINUS_ARROW to=Identifier L_BRACKET functionsList? R_BRACKET SEMICOLON
   ;

functionsList
   :   functionsListPart (COMMA functionsListPart)*
   ;

functionsListPart
   :   name=Identifier (L_BRACKET Identifier? (COMMA Identifier)* R_BRACKET)?
   ;

/* variable declaration with optional initializers
 * syntax: var NAME [= { new AutomatonName(args); atomic }]
 */
variableDecl
   :   VAR nameWithType SEMICOLON
   |   VAR nameWithType EQ assignmentRight SEMICOLON
   ;

nameWithType
   :   name=Identifier COLON type=typeIdentifier
   ;

/*
 * syntax: one.two.three<T>
 */
typeIdentifier
   :   (asterisk=ASTERISK)? name=periodSeparatedFullName (L_ARROW generic=typeIdentifier R_ARROW)?
   ;

variableAssignment
   :   qualifiedAccess EQ assignmentRight SEMICOLON
   ;

assignmentRight
   :   expression
   |   NEW callAutomatonConstructorWithNamedArgs
   ;

callAutomatonConstructorWithNamedArgs
   :   name=periodSeparatedFullName L_BRACKET (namedArgs)? R_BRACKET
   ;

namedArgs
   :   argPair (COMMA argPair)*
   ;

argPair
   :   name=STATE EQ expressionAtomic
   |   name=Identifier EQ expression
   ;

/*
 * syntax: @Annotation
 *         fun name(@annotation arg1: type, arg2: type, ...) [: type] [preambule] { statement1; statement2; ... }
 * In case of declaring extension-function, name must look like Automaton.functionName
 */
functionDecl
   :   functionAnnotations*?
   FUN (automatonName=periodSeparatedFullName DOT)? functionName=Identifier
   L_BRACKET functionDeclArgList? R_BRACKET
   (COLON functionType=typeIdentifier)? (SEMICOLON | functionPreamble (L_BRACE functionBody R_BRACE)?)
   ;

functionDeclArgList
   :   parameter (COMMA parameter)*
   ;

parameter
   :   functionParamAnnotations*? name=Identifier COLON type=typeIdentifier
   ;

/* annotation
 * syntax: @annotationName(args)
 */

functionAnnotations
   :  AT Identifier (L_BRACKET argsList R_BRACKET)?
   ;

functionParamAnnotations
   :   AT Identifier (L_BRACKET argsList R_BRACKET)?
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
   |   assignsContract
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
   :  ACTION Identifier L_BRACKET argsList R_BRACKET SEMICOLON
   ;

argsList
   :   expression (COMMA expression)*
   ;

/* requires contract
 * syntax: requires [name:] condition
 */
requiresContract
   :   REQUIRES (name=Identifier COLON)? expression SEMICOLON
   ;

/* ensures contract
 * syntax: ensures [name:] condition
 */
ensuresContract
   :   ENSURES (name=Identifier COLON)? expression SEMICOLON
   ;

/* assigns contract
 * syntax: assigns [name:] condition
 */
assignsContract
   :   ASSIGNS (name=Identifier COLON)? expression SEMICOLON
   ;

/*
 * expression
 */
expression
   :   lbracket=L_BRACKET expression rbracket=R_BRACKET
   |   expression op=(ASTERISK | SLASH) expression
   |   expression op=PERCENT expression
   |   expression op=(PLUS | MINUS) expression
   |   op=MINUS expression
   |   op=EXCLAMATION expression
   |   expression op=(EQ | NOT_EQ | LESS_EQ | L_ARROW | GREAT_EQ | R_ARROW) expression
   |   expression op=(AND | OR | XOR) expression
   |   qualifiedAccess apostrophe=APOSTROPHE
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
   |   bool=(TRUE | FALSE)
   ;

qualifiedAccess
   :   periodSeparatedFullName
   |   qualifiedAccess L_SQUARE_BRACKET expressionAtomic R_SQUARE_BRACKET (DOT qualifiedAccess)?
   |   simpleCall DOT qualifiedAccess
   ;

simpleCall
   :   Identifier L_BRACKET Identifier R_BRACKET
   ;

identifierList
   :   Identifier (COMMA Identifier)*
   ;

periodSeparatedFullName
   :   Identifier
   |   Identifier (DOT Identifier)*
   |   BACK_QOUTE Identifier (DOT Identifier)* BACK_QOUTE
   ;

integerNumber
   :   MINUS? Digit+
   |   Digit
   ;

floatNumber
   :  MINUS? Digit+ DOT Digit+
   ;