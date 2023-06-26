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
   |   annotationDecl
   |   actionDecl
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
   :   TYPEALIAS left=typeIdentifier ASSIGN_OP right=typeIdentifier SEMICOLON
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
   :   Identifier ASSIGN_OP integerNumber SEMICOLON
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

/* annotation declaration
 * syntax: annotation Something(
 *             variable1: int = 0,
 *             variable2: int = 1
 *         );
 */
annotationDecl
   :   ANNOTATION name=Identifier annotationDeclParams? SEMICOLON
   ;

annotationDeclParams
   :   L_BRACKET annotationDeclParamsPart (COMMA annotationDeclParamsPart)* (COMMA)? R_BRACKET
   ;

annotationDeclParamsPart
   :   nameWithType (ASSIGN_OP expression)?
   ;

actionDecl
   :   annotationUsage*
   DEFINE ACTION actionName=Identifier L_BRACKET actionDeclParamList? R_BRACKET (COLON actionType=typeIdentifier)? SEMICOLON
   ;

actionDeclParamList
   :   actionParameter (COMMA actionParameter)* (COMMA)?
   ;

actionParameter
   :   annotationUsage* name=Identifier COLON type=typeIdentifier
   ;

/* automaton declaration
 * syntax: [@Annotation1(param: type)
 *         @Annotation2(param: type]
 *         automaton Name [(constructor vars)] : type { statement1; statement2; ... }
 */
automatonDecl
   :   annotationUsage* AUTOMATON name=periodSeparatedFullName (L_BRACKET constructorVariables* R_BRACKET)?
   COLON type=periodSeparatedFullName L_BRACE automatonStatement* R_BRACE
   ;

constructorVariables
   :   annotationUsage* keyword=(VAR|VAL) nameWithType (COMMA)?
   ;

automatonStatement
   :   automatonStateDecl
   |   automatonShiftDecl
   |   constructorDecl
   |   destructorDecl
   |   procDecl
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
   :   SHIFT from=Identifier MINUS_ARROW to=Identifier BY functionsListPart SEMICOLON
   |   SHIFT from=Identifier MINUS_ARROW to=Identifier BY L_SQUARE_BRACKET functionsList? R_SQUARE_BRACKET SEMICOLON
   |   SHIFT from=L_BRACKET identifierList R_BRACKET MINUS_ARROW to=Identifier  BY functionsListPart SEMICOLON
   |   SHIFT from=L_BRACKET identifierList R_BRACKET MINUS_ARROW to=Identifier BY L_SQUARE_BRACKET functionsList? R_SQUARE_BRACKET SEMICOLON
   ;

functionsList
   :   functionsListPart (COMMA functionsListPart)* (COMMA)?
   ;

functionsListPart
   :   name=Identifier (L_BRACKET Identifier? (COMMA Identifier)* R_BRACKET)?
   ;

/* variable declaration with optional initializers
 * syntax: var NAME [= { new AutomatonName(args); atomic }]
 */
variableDecl
   :   annotationUsage* keyword=(VAR|VAL) nameWithType SEMICOLON
   |   annotationUsage* keyword=(VAR|VAL) nameWithType ASSIGN_OP assignmentRight SEMICOLON
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
   :   qualifiedAccess op=ASSIGN_OP assignmentRight SEMICOLON
   |   qualifiedAccess op=(PLUS_EQ | MINUS_EQ | ASTERISK_EQ | SLASH_EQ | PERCENT_EQ) assignmentRight SEMICOLON
   |   qualifiedAccess op=(AMPERSAND_EQ | OR_EQ | XOR_EQ) assignmentRight SEMICOLON
   |   qualifiedAccess op=(R_SHIFT_EQ | L_SHIFT_EQ) assignmentRight SEMICOLON
   ;

assignmentRight
   :   expression
   |   callAutomatonConstructorWithNamedArgs
   ;

callAutomatonConstructorWithNamedArgs
   :   NEW name=periodSeparatedFullName L_BRACKET (namedArgs)? R_BRACKET
   ;

namedArgs
   :   argPair (COMMA argPair)*
   ;

argPair
   :   name=STATE ASSIGN_OP expressionAtomic
   |   name=Identifier ASSIGN_OP expression
   ;

constructorDecl
   :   annotationUsage* CONSTRUCTOR functionName=Identifier? L_BRACKET functionDeclArgList? R_BRACKET
   (COLON functionType=typeIdentifier)? (SEMICOLON | L_BRACE functionBody R_BRACE)
   ;

destructorDecl
   :   annotationUsage* DESTRUCTOR functionName=Identifier? L_BRACKET functionDeclArgList? R_BRACKET
   (SEMICOLON | L_BRACE functionBody R_BRACE)?
   ;

procDecl
   :   annotationUsage* PROC functionName=Identifier L_BRACKET functionDeclArgList? R_BRACKET
   (COLON functionType=typeIdentifier)? (SEMICOLON | L_BRACE functionBody R_BRACE)
   ;

/*
 * syntax: @Annotation
 *         fun name(@annotation arg1: type, arg2: type, ...) [: type] [preambule] { statement1; statement2; ... }
 * In case of declaring extension-function, name must look like Automaton.functionName
 */
functionDecl
   :   annotationUsage* FUN (automatonName=periodSeparatedFullName DOT)? functionName=Identifier
   L_BRACKET functionDeclArgList? R_BRACKET (COLON functionType=typeIdentifier)?
   (SEMICOLON | (L_BRACE functionBody R_BRACE)?)
   ;

functionDeclArgList
   :   parameter (COMMA parameter)*
   ;

parameter
   :   annotationUsage* name=Identifier COLON type=typeIdentifier
   ;

/* annotation
 * syntax: @annotationName(args)
 */
annotationUsage
   :   AT Identifier (L_BRACKET expressionsList R_BRACKET)?
   ;

/*
 * declarations between function's header and body-block
 */

functionContract
   :   requiresContract
   |   ensuresContract
   |   assignsContract
   ;

functionBody
   :   functionContract* functionBodyStatements*
   ;

functionBodyStatements
   :   variableAssignment
   |   variableDecl
   |   ifStatement
   |   expression SEMICOLON
   ;

ifStatement
   :   IF expression L_BRACE functionBodyStatements* R_BRACE (elseStatement)?
   ;

elseStatement
   :   ELSE L_BRACE functionBodyStatements* R_BRACE
   ;

/* semantic action
 * syntax: action ActionName(args)
 */
actionUsage
   :  ACTION Identifier L_BRACKET expressionsList? R_BRACKET
   ;

procUsage
   :  qualifiedAccess L_BRACKET expressionsList? R_BRACKET
   ;

expressionsList
   :   expression (COMMA expression)* (COMMA)?
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
   |   expression op=(EQ | EXCLAMATION_EQ | L_ARROW_EQ | L_ARROW | R_ARROW_EQ | R_ARROW) expression
   |   expression op=(AMPERSAND | BIT_OR | XOR) expression
   |   expression op=(DOUBLE_AMPERSAND | LOGIC_OR) expression
   |   expression bitShiftOp expression
   |   qualifiedAccess apostrophe=APOSTROPHE
   |   expressionAtomic
   |   qualifiedAccess
   |   unaryOp
   |   procUsage
   |   actionUsage
   ;

bitShiftOp
   :   lShift
   |   rShift
   |   uRShift
   ;

lShift
   :   L_ARROW L_ARROW
   ;

rShift
   :   R_ARROW R_ARROW
   ;

uRShift
   :   R_ARROW R_ARROW R_ARROW
   ;

unaryOp
   :   op=PLUS expression
   |   op=MINUS expression
   |   op=EXCLAMATION expression
   |   op=TILDE expression
   ;

expressionAtomic
   :   qualifiedAccess
   |   primitiveLiteral
   |   arrayLiteral
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

arrayLiteral
   :   L_SQUARE_BRACKET expressionsList? R_SQUARE_BRACKET
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
