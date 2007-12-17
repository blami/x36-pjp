/*
 * 	Token.java
 *	Vycet tokenu rozpoznavanych tridou Lexer.
 *
 *	Autor: Ondøej Baláž <ondra@blami.net>
 *	X36PJP, èt 14:30
 */
package x36pjp;

public enum Token
{
	/* neterminaly */
	T_IDENTIFIER, 			/* identifier: 	letter { letter | digit } */
	T_INTEGER,				/* integer:		digit { digit } */
	T_REAL,					/* real:		integer '.' integer */
	
	/* terminaly */
	T_OBRACKET,				/* ( */
	T_CBRACKET,				/* ) */
	T_COMMA,				/* , */
	T_COLON,				/* : */
	T_SEMICOLON,			/* ; */
	T_ASSIGN,				/* := */
	T_ADD,					/* + */
	T_SUB,					/* - */
	T_MUL,					/* * */
	T_DIV,					/* / */
	T_EQUAL,				/* = */
	T_DIFFER,				/* <> */
	T_LESS,					/* < */
	T_GREAT,				/* > */
	T_LESSEQ,				/* <= */
	T_GREATEQ,				/* >= */
	T_DOT,					/* . */
	T_KW_PROGRAM,			
	T_KW_VAR,
	T_KW_INTEGER,
	T_KW_REAL,
	T_KW_BEGIN,
	T_KW_END,
	T_KW_DIV,
	T_KW_MOD,
	T_KW_AND,
	T_KW_OR,
	T_KW_IF,
	T_KW_THEN,
	T_KW_ELSE,
	T_KW_WHILE,
	T_KW_DO,
	T_KW_FOR,
	T_KW_TO,
	T_KW_DOWNTO,
	T_KW_WRITE,
	T_KW_WRITELN,	
	
	/* berlicky */ 
	T_EOF					/* end of file */
}
