/*-***
 *
 * This file defines a stand-alone lexical analyzer for a subset of the Pascal
 * programming language.  This is the same lexer that will later be integrated
 * with a CUP-based parser.  Here the lexer is driven by the simple Java test
 * program in ./PascalLexerTest.java, q.v.  See 330 Lecture Notes 2 and the
 * Assignment 2 writeup for further discussion.
 *
 */

package scanner;
import java_cup.runtime.*;


%%
/*-*
 * LEXICAL FUNCTIONS:
 */

%cup
%line
%column
%unicode
%class PascalPPLexer

%{

/**
 * Return a new Symbol with the given token id, and with the current line and
 * column numbers.
 */
Symbol newSym(int tokenId) {
    return new Symbol(tokenId, yyline, yycolumn);
}

/**
 * Return a new Symbol with the given token id, the current line and column
 * numbers, and the given token value.  The value is used for tokens such as
 * identifiers and numbers.
 */
Symbol newSym(int tokenId, Object value) {
    return new Symbol(tokenId, yyline, yycolumn, value);
}

%}


/*-*
 * PATTERN DEFINITIONS:
 */
letter          = [A-Za-z]
digit           = [0-9]
alphanumeric    = {letter}|{digit}
other_id_char   = [_]
identifier      = {letter}({alphanumeric}|{other_id_char})*
integer         = {digit}*
real            = {integer}\.{integer}
char            = '.'
nonrightEC   	= [^(\-\->)]
comment_body    = {nonrightEC}*
nonrightE		= [^\n]
comment_b		= {nonrightE}*
comment         = [<][\-][\-]{comment_body}[\-][\-][>]|[\-][\-]{comment_b}[\n]
whitespace      = [ \n\t\f\r\v]*
string			= \"([^\"\\\\]|\\\\.)*\"
boolean         = true|false

%%
/**
 * LEXICAL RULES:
 */
boolean			{ return newSym(sym.BOOL_KW); }
char			{ return newSym(sym.CHAR_KW); }
integer			{ return newSym(sym.INT_KW); }
real			{ return newSym(sym.REAL_KW); }
string			{ return newSym(sym.STRING_KW); }
function		{ return newSym(sym.FUNC); }
return			{ return newSym(sym.RETURN); }
while			{ return newSym(sym.WHILE); }
do				{ return newSym(sym.DO); }
read			{ return newSym(sym.READ); }
write			{ return newSym(sym.WRITE); }
{boolean}		{ return newSym(sym.BOOL, yytext()); }
{string}		{ return newSym(sym.STRING, yytext()); }
"~"				{ return newSym(sym.L_NOT); }
"%"				{ return newSym(sym.MOD); }
"|"				{ return newSym(sym.B_OR); }
"&"				{ return newSym(sym.B_AND); }
"^"				{ return newSym(sym.XOR);  }
begin           { return newSym(sym.BEGIN); }
and             { return newSym(sym.L_AND); }
array           { return newSym(sym.ARRAY); }
else            { return newSym(sym.ELSE); }
end             { return newSym(sym.END); }
if              { return newSym(sym.IF); }
of              { return newSym(sym.OF); }
or              { return newSym(sym.L_OR); }
program         { return newSym(sym.PROGRAM); }
procedure       { return newSym(sym.PROCEDURE); }
then            { return newSym(sym.THEN); }
type            { return newSym(sym.TYPE); }
var             { return newSym(sym.VAR); }
"*"             { return newSym(sym.TIMES); }
"+"             { return newSym(sym.PLUS); }
"-"             { return newSym(sym.MINUS); }
"/"             { return newSym(sym.DIVIDE); }
";"             { return newSym(sym.SEMI); }
","             { return newSym(sym.COMMA); }
"("             { return newSym(sym.LEFT_PAREN); }
")"             { return newSym(sym.RT_PAREN); }
"["             { return newSym(sym.LEFT_BRKT); }
"]"             { return newSym(sym.RT_BRKT); }
"="             { return newSym(sym.EQ); }
"<"             { return newSym(sym.GTR); }
">"             { return newSym(sym.LESS); }
"<="            { return newSym(sym.LESS_EQ); }
">="            { return newSym(sym.GTR_EQ); }
"<>"            { return newSym(sym.NOT_EQ); }
":"             { return newSym(sym.COLON); }
":="            { return newSym(sym.ASSMNT); }
"."             { return newSym(sym.DOT); }
{identifier}    { return newSym(sym.ID, yytext()); }
{integer}       { return newSym(sym.INT, new Integer(yytext())); }
{real}          { return newSym(sym.REAL, new Double(yytext())); }
{char}          { return newSym(sym.CHAR, new Character(yytext().charAt(1))); }
{comment}       { /* For this stand-alone lexer, print out comments. */
                  System.out.println("Recognized comment: " + yytext()); }
{whitespace}    { /* Ignore whitespace. */ }
.               { System.out.println("Illegal char, '" + yytext() +
                    "' line: " + yyline + ", column: " + yychar); }
