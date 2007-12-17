/*
 * 	SyntaxAnalyser.java
 *	Syntakticky analyzer pro Pascal (rekurzivni sestup).
 *
 *	Autor: Ondøej Baláž <ondra@blami.net>
 *	X36PJP, èt 14:30
 */
package x36pjp;
import java.io.*;
import java.util.*;


public class SyntaxAnalyzer
{
	private LexicalAnalyzer lexicalAnalyzer = null;
	private Token token = null;
	
	/* tabulka s popisy tokenu */
	public static Hashtable<Token, String> tokenDesc = 
		LexicalAnalyzer.buildElementsOutputTable();
	
	
	/**Vytvori novy objekt syntaktickeho analyzeru nad existujicim lexikalnim
	 * analyzerem.
	 * @param lexer		Lexikalni analyzer nad zdrojovym souborem.
	 * @throws IOException */
	public SyntaxAnalyzer(LexicalAnalyzer lexer) 
	throws IOException
	{
		this.lexicalAnalyzer = lexer;
		token = readNextToken();
	}
	
	
	/**Provede syntaktickou analyzu zdrojoveho souboru.
	 * @throws IOException */
	public void analyzeSource()
	throws IOException
	{
		Program();
		if(this.token != Token.T_EOF)
			System.err.println("CHYBA: neocekavany znak na vstupu!");
	}
	
	
	/**Nacte dalsi token z lexikalniho analyzeru.
	 * @return			Token ktery byl nacten (a ulozen jako aktualni).
	 * @throws IOException */
	private Token readNextToken() 
	throws IOException
	{
		this.token = this.lexicalAnalyzer.nextToken();
		return this.token;	
	}
	
	
	/**Srovna predany token s "aktualnim" tokenem.
	 * @param token		Token, ktery ma byt porovnan s "aktualnim" tokenem. */
	private void compare(Token token)
	throws IOException
	{
		if(this.token == token)
		{
			System.out.println("Srovnani (" + tokenDesc.get(this.token) + "," + 
					tokenDesc.get(token) + ") souhlasi.");
			readNextToken();
		}
		else
		{
			System.err.println("CHYBA: srovnani (" + 
					tokenDesc.get(this.token) + "," +
					tokenDesc.get(token) + ") nesouhlasi!");
		}
	}
		
	
	/* -- zacatek expanznich funkci -- */
	
	
	private void Program()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_PROGRAM:
			System.out.println("1 Program -> program identifikator ; Blok .");
			compare(Token.T_KW_PROGRAM);
			compare(Token.T_IDENTIFIER);
			compare(Token.T_SEMICOLON);
			Blok();
			compare(Token.T_DOT);
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat Program!");
		}
	}	
	
	
	private void Blok()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_BEGIN:
			/* 2 UsekDeklaraciPromennych SlozenyPrikaz */
			UsekDeklaraciPromennych();
			SlozenyPrikaz();
			break;
		case T_KW_VAR:
			/* 2 UsekDeklaraciPromennych SlozenyPrikaz */
			UsekDeklaraciPromennych();
			SlozenyPrikaz();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat Blok!");
		}
	}	
	
	
	private void UsekDeklaraciPromennych()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_VAR:
			System.out.println("3 UsekDeklaraciPromennch -> var DeklaracePromennych ; ZbytekDeklaraciPromennych");
			compare(Token.T_KW_VAR);
			DeklaracePromennych();
			compare(Token.T_SEMICOLON);
			ZbytekDeklaraciPromennych();
			break;		
		case T_KW_BEGIN:
			System.out.println("4 UsekDeklaraciPromennch -> e");
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat UsekDeklaraciPromennych!");
		}
	}	
	
	
	private void DeklaracePromennych()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			System.out.println("5 DeklaracePromennch -> SeznamIdentifikatoru : OznaceniTypu");
			SeznamIdentifikatoru();
			compare(Token.T_COLON);
			OznaceniTypu();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat DeklaracePromennych!");
		}
	}
	
	
	private void SeznamIdentifikatoru()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			System.out.println("6 SeznamIdentifikatoru -> identifikator ZbytekSeznamuIdentifikatoru");
			compare(Token.T_IDENTIFIER);
			ZbytekSeznamuIdentifikatoru();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat SeznamIdentifikatoru!");
		}
	}		

	
	private void ZbytekSeznamuIdentifikatoru()
	throws IOException
	{
		switch(this.token)
		{
		case T_COMMA:
			System.out.println("7 ZbytekSeznamuIdentifikatoru -> , SeznamIdentifikatoru");
			compare(Token.T_COMMA);
			SeznamIdentifikatoru();
			break;
		case T_COLON:
			System.out.println("8 ZbytekSeznamuIdentifikatoru -> e");
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat SeznamIdentifikatoru!");
		}
	}	
	
	
	private void OznaceniTypu()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_INTEGER:
			System.out.println("9 OznaceniTypu -> integer");
			compare(Token.T_KW_INTEGER);
			break;		
		case T_KW_REAL:
			System.out.println("10 OznaceniTypu -> real");
			compare(Token.T_KW_REAL);
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat OznaceniTypu!");
		}
	}
	
	
	private void ZbytekDeklaraciPromennych()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			System.out.println("11 ZbytekDeklaraciPromennych -> DeklaracePromennych ; ZbytekDeklaraciPromennych");
			DeklaracePromennych();
			compare(Token.T_SEMICOLON);
			ZbytekDeklaraciPromennych();
			break;				
		case T_KW_BEGIN:
			System.out.println("12 ZbytekDeklaraciPromennych -> e");
			break;		
		default:
			System.err.println("CHYBA: Nelze expandovat ZbytekDeklaraciPromennych!");
		}		
	}
	
	
	private void SlozenyPrikaz()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_BEGIN:
			System.out.println("13 SlozenyPrikaz -> begin PosloupnostPrikazu end");
			compare(Token.T_KW_BEGIN);
			PosloupnostPrikazu();
			compare(Token.T_KW_END);
			break;		
		default:
			System.err.println("CHYBA: Nelze expandovat SlozenyPrikaz!");
		}
	}
	
	
	private void PosloupnostPrikazu()
	throws IOException
	{
		switch(this.token)
		{
		case T_SEMICOLON:
		case T_KW_BEGIN:
		case T_KW_END:
		case T_KW_FOR:
		case T_IDENTIFIER:
		case T_KW_IF:
		case T_KW_WHILE:
		case T_KW_WRITE:
		case T_KW_WRITELN:
			System.out.println("14 PosloupnostPrikazu -> Prikaz ZbytekPosloupnostiPrikazu");
			Prikaz();
			ZbytekPosloupnostiPrikazu();
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat PosloupnostPrikazu!");
		}
	}		
	
	
	private void Prikaz()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			System.out.println("15 Prikaz -> PrirazovaciPrikaz");
			PrirazovaciPrikaz();
			break;
		case T_KW_BEGIN:
			System.out.println("16 Prikaz -> SlozenyPrikaz");
			SlozenyPrikaz();
			break;			
		case T_KW_IF:
			System.out.println("17 Prikaz -> PrikazIf");
			PrikazIf();
			break;		
		case T_KW_WHILE:
			System.out.println("18 Prikaz -> PrikazWhile");
			PrikazWhile();
			break;
		case T_KW_FOR:
			System.out.println("19 Prikaz -> PrikazFor");
			PrikazFor();
			break;				
		case T_KW_WRITELN:
			System.out.println("20 Prikaz -> PrikazWriteln");
			PrikazWriteln();
			break;		
		case T_SEMICOLON:
			System.out.println("21 Prikaz -> PrazdnyPrikaz");
			PrazdnyPrikaz();
			break;		
		case T_KW_ELSE:
			System.out.println("21 Prikaz -> PrazdnyPrikaz");
			SlozenyPrikaz();
			break;			
		case T_KW_END:
			System.out.println("21 Prikaz -> PrazdnyPrikaz");
			PrazdnyPrikaz();
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat Prikaz!");
		}
	}	
	
	
	private void PrazdnyPrikaz()
	throws IOException
	{
		switch(this.token)
		{
		case T_SEMICOLON:	
		case T_KW_ELSE:
		case T_KW_END:
			System.out.println("22 PrazdnyPrikaz -> e");
			break;		
		default:
			System.err.println("CHYBA: Nelze expandovat PrazdnyPrikaz!");
		}
	}	
	
	
	private void PrirazovaciPrikaz()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			System.out.println("23 PrirazovaciPrikaz -> identifikator := Prikaz");
			compare(Token.T_IDENTIFIER);
			compare(Token.T_ASSIGN);
			Vyraz();
			break;		
		default:
			System.err.println("CHYBA: Nelze expandovat PrirazovaciPrikaz!");
		}
	}		
	
	
	private void Vyraz()
	throws IOException
	{
		switch(this.token)
		{		
		case T_OBRACKET:
		case T_ADD:
		case T_SUB:
		case T_INTEGER:
		case T_REAL:
		case T_IDENTIFIER:
			System.out.println("24 Vyraz -> Znamenko Term ZbytekVyrazu");
			Znamenko();
			Term();
			ZbytekVyrazu();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat Vyraz!");
		}
	}		
	
	
	private void Znamenko()
	throws IOException
	{
		switch(this.token)
		{
		case T_ADD:
			System.out.println("25 Znamenko -> +");
			compare(Token.T_ADD);
			break;
		case T_SUB:
			System.out.println("26 Znamenko -> -");
			compare(Token.T_SUB);
			break;			
		case T_INTEGER:
		case T_REAL:
		case T_IDENTIFIER:
		case T_OBRACKET:
			System.out.println("27 Znamenko -> e");
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat Znamenko!");
		}
	}	
	
	
	private void Term()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
		case T_INTEGER:
		case T_REAL:	
		case T_OBRACKET:
			System.out.println("28 Term -> Faktor ZbytekTermu");
			Faktor();
			ZbytekTermu();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat Term!");
		}
	}	
	
	
	private void Faktor()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			System.out.println("29 Faktor -> identifikator");
			compare(Token.T_IDENTIFIER);
			break;
		case T_INTEGER:
			System.out.println("30 Faktor -> celeCislo");
			compare(Token.T_INTEGER);
			break;
		case T_REAL:
			System.out.println("31 Faktor -> realneCislo");
			compare(Token.T_REAL);
			break;			
		case T_OBRACKET:
			System.out.println("32 Faktor -> ( Vyraz )");
			compare(Token.T_OBRACKET);
			Vyraz();
			compare(Token.T_CBRACKET);
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat Faktor!");
		}
	}		
	
	
	private void ZbytekTermu()
	throws IOException
	{
		switch(this.token)
		{
		case T_DIV:
		case T_MUL:
		case T_KW_DIV:
		case T_KW_MOD:
			System.out.println("33 ZbytekTermu -> MultiplikativniOperator Faktor ZbytekTermu");
			MultiplikativniOperator();
			Faktor();
			ZbytekTermu();
			break;			
		case T_ADD:
		case T_SUB:
		case T_SEMICOLON:
		case T_CBRACKET:
		case T_LESS:
		case T_LESSEQ:
		case T_DIFFER:
		case T_EQUAL:
		case T_GREAT:
		case T_GREATEQ:
		case T_KW_DO:
		case T_KW_DOWNTO:
		case T_KW_ELSE:
		case T_KW_END:
		case T_KW_THEN:
		case T_KW_TO:
			System.out.println("34 ZbytekTermu -> e");
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat ZbytekTermu!");
		}
	}		
	
	
	private void MultiplikativniOperator()
	throws IOException
	{
		switch(this.token)
		{
		case T_MUL:
			System.out.println("35 MultiplikativniOperator -> *");
			compare(Token.T_MUL);
			break;
		case T_DIV:
			System.out.println("36 MultiplikativniOperator -> /");
			compare(Token.T_DIV);
			break;
		case T_KW_DIV:
			System.out.println("37 MultiplikativniOperator -> div");
			compare(Token.T_KW_DIV);
			break;
		case T_KW_MOD:
			System.out.println("38 MultiplikativniOperator -> mod");
			compare(Token.T_KW_MOD);
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat MultiplikativniOperator!");
		}
	}			
	

	private void ZbytekVyrazu()
	throws IOException
	{
		switch(this.token)
		{
		case T_ADD:
		case T_SUB:
			System.out.println("39 ZbytekVyrazu -> AditivniOperator Term ZbytekVyrazu");
			AditivniOperator();
			Term();
			ZbytekVyrazu();
			break;			
		case T_CBRACKET:
		case T_SEMICOLON:
		case T_LESS:
		case T_LESSEQ:
		case T_DIFFER:
		case T_EQUAL:
		case T_GREAT:
		case T_GREATEQ:
		case T_KW_DO:
		case T_KW_DOWNTO:
		case T_KW_ELSE:
		case T_KW_END:
		case T_KW_THEN:
		case T_KW_TO:
			System.out.println("40 ZbytekVyrazu -> e");
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat ZbytekVyrazu!");
		}
	}	
	
	
	private void AditivniOperator()
	throws IOException
	{
		switch(this.token)
		{
		case T_ADD:
			System.out.println("41 AdditivniOperator -> +");			
			compare(Token.T_ADD);
			break;
		case T_SUB:
			System.out.println("42 AdditivniOperator -> -");			
			compare(Token.T_SUB);
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat AdditivniOperator!");
		}
	}
	

	private void ZbytekPosloupnostiPrikazu()
	throws IOException
	{
		switch(this.token)
		{
		case T_SEMICOLON:
			System.out.println("43 ZbytekPosloupnostiPrikazu -> ; Prikaz ZbytekPosloupnostiPrikazu");
			compare(Token.T_SEMICOLON);
			Prikaz();
			ZbytekPosloupnostiPrikazu();
			break;
		case T_KW_END:
			System.out.println("44 ZbytekPosloupnostiPrikazu -> e");
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat ZbytekPosloupnostiPrikazu!");
		}
	}			
	
	
	private void PrikazIf()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_IF:
			System.out.println("45 PrikazIf -> if Podminka then Prikaz CastElse");
			compare(Token.T_KW_IF);
			Podminka();
			compare(Token.T_KW_THEN);
			Prikaz();
			CastElse();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat PrikazIf!");
		}
	}		
	

	private void Podminka()
	throws IOException
	{
		switch(this.token)
		{
		case T_OBRACKET:	
		case T_ADD:	
		case T_SUB:	
		case T_INTEGER:	
		case T_REAL:	
		case T_IDENTIFIER:
			System.out.println("46 Podminka -> Vyraz RelacniOperator Vyraz");
			Vyraz();
			RelacniOperator();
			Vyraz();
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat Podminka!");
		}
	}	

	
	private void RelacniOperator()
	throws IOException
	{
		switch(this.token)
		{
		case T_LESS:
			System.out.println("49 RelacniOperator -> <");
			compare(Token.T_LESS);
			break;	
		case T_LESSEQ:
			System.out.println("51 RelacniOperator -> <=");
			compare(Token.T_LESSEQ);
			break;
		case T_DIFFER:
			System.out.println("48 RelacniOperator -> <>");
			compare(Token.T_DIFFER);
			break;
		case T_EQUAL:
			System.out.println("47 RelacniOperator -> =");
			compare(Token.T_EQUAL);
			break;
		case T_GREAT:
			System.out.println("50 RelacniOperator -> >");
			compare(Token.T_GREAT);
			break;
		case T_GREATEQ:
			System.out.println("50 RelacniOperator -> >=");
			compare(Token.T_GREATEQ);
			break;				
		default:
			System.err.println("CHYBA: Nelze expandovat RelacniOperator!");
		}
	}	
	
	
	private void CastElse()
	throws IOException
	{
		switch(this.token)
		{
		case T_SEMICOLON:
			System.out.println("54 CastElse -> e");
			break;
		case T_KW_ELSE:
			System.out.println("53 CastElse -> else Prikaz");
			compare(Token.T_KW_ELSE);
			Prikaz();
		case T_KW_END:
			System.out.println("54 CastElse -> e");
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat CastElse!");
		}
	}
	
	
	private void PrikazWhile()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_WHILE:
			System.out.println("55 PrikazWhile -> while Podminka do Prikaz");
			compare(Token.T_KW_WHILE);
			Podminka();
			compare(Token.T_KW_DO);
			Prikaz();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat PrikazWhile!");
		}
	}	
	
	
	private void PrikazFor()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_FOR:
			System.out.println("56 PrikazFor -> for identifikator := Vyraz CastFor Vyraz do Prikaz");
			compare(Token.T_KW_FOR);
			compare(Token.T_IDENTIFIER);
			compare(Token.T_ASSIGN);
			Vyraz();
			CastFor();
			Vyraz();
			compare(Token.T_KW_DO);
			Prikaz();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat PrikazFor!");
		}
	}		
	
	
	private void CastFor()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_DOWNTO:
			System.out.println("58 CastFor -> downto");
			compare(Token.T_KW_DOWNTO);
			break;
		case T_KW_TO:
			System.out.println("57 CastFor -> to");
			compare(Token.T_KW_TO);
			break;			
		default:
			System.err.println("CHYBA: Nelze expandovat CastFor!");
		}
	}	
	
	
	private void PrikazWriteln()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_WRITELN:
			System.out.println("59 PrikazWriteln -> writeln ( Vyraz )");
			compare(Token.T_KW_WRITELN);
			compare(Token.T_OBRACKET);
			Vyraz();
			compare(Token.T_CBRACKET);
			Prikaz();
			break;
		default:
			System.err.println("CHYBA: Nelze expandovat PrikazWriteln!");
		}
	}	
	
	
	/** -- konec expanznich funkci --  **/
	
	
	/**Vstupni bod do programu (self-test). 
	 * @param args		Pole argumentu predanych prikazovou radkou. V uvahu se 
	 * 					bere index <code>1</code> ktery obsahuje cestu ke 
	 * 					vstupnimu souboru.*/
	public static void main(String args[]) throws IOException
	{
		LexicalAnalyzer lexicalAnalyser = null;
		
		try
		{
			if(args.length == 1)
				lexicalAnalyser = 
					new LexicalAnalyzer(new FileInputStream(args[0]));
		}
		catch (FileNotFoundException e)
		{
			lexicalAnalyser = null;
		}
		
		if(lexicalAnalyser == null)
		{
			System.out.println("Vstup ze stdin:");
			lexicalAnalyser = new LexicalAnalyzer(System.in);
		}
		else 
		{
			System.out.println("Vstup ze souboru " + args[0]);
		}	
		
		SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer(lexicalAnalyser);
		syntaxAnalyzer.analyzeSource();
	}
}
