1.KB - lexikalni analyzer stripped-down pascalu
===============================================

lexikalni gramatika:

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

  | Element:	| Token:	
--------------------------------------
N |identifier	| T_IDENTIFIER 
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


2. KB - syntakticky analyzer stripped down pascalu
==================================================

vstupni gramatika:

01 Program -> program identifikator ; Blok .
02 Blok -> UsekDeklaraciPromennych SlozenyPrikaz
03 UsekDeklaraciPromennych -> var DeklaracePromennych ; ZbytekDeklaraciPromennych
04 UsekDeklaraciPromennych -> e
05 DeklaracePromennych -> SeznamIdentifikatoru : OznaceniTypu
06 SeznamIdentifikatoru -> identifikator ZbytekSeznamuIdentifikatoru
07 ZbytekSeznamuIdentifikatoru -> , SeznamIdentifikatoru
08 ZbytekSeznamuIdentifikatoru -> e
09 OznaceniTypu -> integer
10 OznaceniTypu -> real
11 ZbytekDeklaraciPromennych -> DeklaracePromennych ; ZbytekDeklaraciPromennych
12 ZbytekDeklaraciPromennych -> e
13 SlozenyPrikaz -> begin PosloupnostPrikazu end
14 PosloupnostPrikazu -> Prikaz ZbytekPosloupnostiPrikazu
15 Prikaz -> PrirazovaciPrikaz
16 Prikaz -> SlozenyPrikaz
17 Prikaz -> PrikazIf
18 Prikaz -> PrikazWhile
19 Prikaz -> PrikazFor
20 Prikaz -> PrikazWriteln
21 Prikaz -> PrazdnyPrikaz
22 PrazdnyPrikaz -> e
23 PrirazovaciPrikaz -> identifikator := Vyraz
24 Vyraz -> Znamenko Term ZbytekVyrazu
25 Znamenko -> +
26 Znamenko -> -
27 Znamenko -> e
28 Term -> Faktor ZbytekTermu
29 Faktor -> identifikator
30 Faktor -> celeCislo
31 Faktor -> realneCislo
32 Faktor -> ( Vyraz )
33 ZbytekTermu -> MultiplikativniOperator Faktor ZbytekTermu
34 ZbytekTermu -> e
35 MultiplikativniOperator -> *
36 MultiplikativniOperator -> /
37 MultiplikativniOperator -> div
38 MultiplikativniOperator -> mod
39 ZbytekVyrazu -> AditivniOperator Term ZbytekVyrazu
40 ZbytekVyrazu -> e
41 AditivniOperator -> +
42 AditivniOperator -> -
43 ZbytekPosloupnostiPrikazu -> ; Prikaz ZbytekPosloupnostiPrikazu
44 ZbytekPosloupnostiPrikazu -> e
45 PrikazIf -> if Podminka then Prikaz CastElse
46 Podminka -> Vyraz RelacniOperator Vyraz
47 RelacniOperator -> =
48 RelacniOperator -> <>
49 RelacniOperator -> <
50 RelacniOperator -> >
51 RelacniOperator -> <=
52 RelacniOperator -> >=
53 CastElse -> else Prikaz
54 CastElse -> e
55 PrikazWhile -> while Podminka do Prikaz
56 PrikazFor -> for identifikator := Vyraz CastFor Vyraz do Prikaz
57 CastFor -> to
58 CastFor -> downto
59 PrikazWriteln -> writeln ( Vyraz )


3. KB - prekladac stripped down pascalu
=======================================

prekladova atributova gramatika:

dedicne atributy:
NEG: dtyp
ZbytekVyrazu: levy
ZbytekTermu: levy
LDC: hodn
LDC2_W: hodn
Operace: levy, pravy
InvokeVirtual: dtyp

syntetizovane atributy:
Vyraz: typ
ZbytekVyrazu: typ
Znamenko: znam
Term: typ
Faktor: typ
ZbytekTermu: typ
MultiplikativniOperator: oper
AditivniOperator: oper
Operace: typ

24 Vyraz -> Znamenko Term <NEG> ZbytekVyrazu
    	NEG.dtyp = Term.typ
    	ZbytekVyrazu.levy = Term.typ
    	Vyraz.typ = ZbytekVyrazu.typ
25 Znamenko -> +
    	Znamenko.znam = kladne
26 Znamenko -> -
    	Znamenko.znam = zaporne
27 Znamenko -> e
    	Znamenko.znam = kladne
28 Term -> Faktor ZbytekTermu
    	ZbytekTermu.levy = Faktor.typ
    	Term.typ = ZbytekTermu.typ
pravidlo 29 doplnime pozdeji
30 Faktor -> celeCislo <LDC>
    	LDC.hodn = celeCislo.hodn
    	Faktor.typ = integer
31 Faktor -> realneCislo <LDC2_W>
    	LDC2_W.hodn = realneCislo.hodn
    	Faktor.typ = real
32 Faktor -> ( Vyraz )
    	Faktor.typ = Vyraz.typ
33 ZbytekTermu0 -> MultiplikativniOperator Faktor <Operace> ZbytekTermu1
    	Operace.levy = ZbytekTermu0.levy
    	Operace.pravy = Faktor.typ
    	if (Operace.levy != Operace.pravy) chyba (nebo konverze i2d)    
    	Operace.oper = MultiplikativniOperator.oper
    	ZbytekTermu1.levy = Operace.typ
    	ZbytekTermu0.typ = ZbytekTermu1.typ
34 ZbytekTermu -> e
    	ZbytekTermu.typ = ZbytekTermu.levy
35 MultiplikativniOperator -> *
    	MultiplikativniOperator.oper = mul
36 MultiplikativniOperator -> /
    	MultiplikativniOperator.oper = div
37 MultiplikativniOperator -> div
    	MultiplikativniOperator.oper = idiv
38 MultiplikativniOperator -> mod
    	MultiplikativniOperator.oper = imod
39 ZbytekVyrazu0 -> AditivniOperator Term <Operace> ZbytekVyrazu1
    	Operace.levy = ZbytekVyrazu0.levy
    	Operace.pravy = Term.typ
    	Operace.oper = AditivniOperator.oper
    	if (Operace.levy != Operace.pravy) chyba (nebo konverze i2d)
    	ZbytekVyrazu1.levy = Operace.typ
    	ZbytekVyrazu0.typ = ZbytekVyrazu1.typ
40 ZbytekVyrazu -> e
    	ZbytekVyrazu.typ = ZbytekVyrazu.levy
41 AditivniOperator -> +
    	AditivniOperator.oper = add
42 AditivniOperator -> -
    	AditivniOperator.oper = sub
59 PrikazWriteln -> writeln <GetStatic> ( Vyraz ) <InvokeVirtual>
    	InvokeVirtual.dtyp = Vyraz.typ