1.KB - lexikalni analyzer stripped-down pascalu

element		= identifier | integer | real | spec-symb | keyword
identifier	= letter { letter | digit }
letter		= 'a' | ... | 'z'
digit		= '0' | ... | '9'
keyword     = 'program' | 'var' | 'integer' | 'real' | 'begin' | 'end' | 'div' |
			  'mod' | 'and' | 'or' | 'if' | 'then' | 'else' | 'while' | 'do' |
			  'for' | 'to' | 'downto' | 'write' | 'writeln'
integer		= digit { digit }
real		= integer '.' integer
spec-symb	= '(' | ')' | ',' | ':' | ';' | ':=' | '+' | '-' | '*' | '/' | '=' |
			  '<>' | '<' | '>' | '<=' | '>=' | '.'
		
Komentare { ... }
Case-insensitive

Tokeny:

  | Element:	        | Token:	
--------------------------------------
N |identifier	        | T_IDENTIFIER 
N |integer		| T_INTEGER
N |real			| T_REAL
T |(			| T_OBRACKET
T |)			| T_CBRACKET
T |,			| T_COMMA
T |:			| T_COLON
T |;			| T_SEMICOLON
T |:=			| T_ASSIGN
T |+			| T_ADD
T |-			| T_SUB
T |*			| T_MUL
T |/			| T_DIV
T |=			| T_EQUAL
T |<>			| T_DIFFER
T |<			| T_LESS
T |>			| T_GREAT
T |<=			| T_LESSEQ
T |>=			| T_GREATEQ
T |.			| T_DOT
T |program		| T_KW_PROGRAM
T |var			| T_KW_VAR
T |integer		| T_KW_INTEGER
T |real			| T_KW_REAL
T |begin		| T_KW_BEGIN
T |end			| T_KW_END
T |div			| T_KW_DIV
T |mod			| T_KW_MOD
T |and			| T_KW_AND
T |or			| T_KW_OR
T |if 			| T_KW_IF
T |then			| T_KW_THEN
T |else			| T_KW_ELSE
T |while		| T_KW_WHILE
T |do			| T_KW_DO
T |for			| T_KW_FOR
T |to			| T_KW_TO
T |downto		| T_KW_DOWNTO
T |write		| T_KW_WRITE
T |writeln		| T_KW_WRITELN
