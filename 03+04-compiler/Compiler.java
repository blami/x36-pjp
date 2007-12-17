/*
 * 	Compiler.java
 *	Kompiler pro Pascal (rekurzivni sestup).
 *
 *	Autor: Ondøej Baláž <ondra@blami.net>
 *	X36PJP, èt 14:30
 */
package x36pjp;
import java.io.*;
import java.util.*;


public class Compiler
{
	private LexicalAnalyzer lexicalAnalyzer = null;
	private Token token = null;
	
	/* prekladac */
	private Hashtable<String, Variable> vars;
	private int lastAddr = 0;
	private String outBuffer;
	
	
	/* tabulka s popisy tokenu */
	public static Hashtable<Token, String> tokenDesc = 
		LexicalAnalyzer.buildElementsOutputTable();

	
	private boolean error = false;
	private boolean debug_terminate = true;
	private boolean debug_verbose = false;
	
	
	/**Vypise ladici hlasku.
	 * @param msg		Text ladici hlasky. */
	private void debugMessage(String msg)
	{
		if(debug_verbose)
			System.out.println(msg);
	}
	
	
	/**Vypise chybovou hlasku.
	 * @param msg		Text ladici hlasky. */	
	private void debugError(String facility, String msg)
	{
		System.err.println("["+facility+"] " + msg);
		error = true;
		
		if(debug_terminate)
			System.exit(1);
	}
		
	
	/**Vytvori novy objekt syntaktickeho analyzeru nad existujicim lexikalnim
	 * analyzerem.
	 * @param lexer		Lexikalni analyzer nad zdrojovym souborem.
	 * @throws IOException */
	public Compiler(LexicalAnalyzer lexer) 
	throws IOException
	{
		this.lexicalAnalyzer = lexer;
		token = readNextToken();
		
		/* pripravime hesovaci tabulku promennych */
		vars = new Hashtable<String, Variable>();
		
		outBuffer = "";
	}
	
	
	/**Prida promennou do tabulky promennych.
	 * @param identifier	Identifikator promenne.
	 * @param type		Datovy typ promenne.
	 * @param address	Relativni adresa promenne. */
	public void addVar(String identifier)
	{
		//System.out.println(lexicalAnalyzer.getTokenValue());
		
		Variable var = new Variable();
		vars.put(identifier, var);	
	}
	
	
	/**Provede syntaktickou analyzu a preklad zdrojoveho souboru.
	 * @throws IOException */
	public String compileSource()
	throws IOException
	{
		error = false;
		
		Program();
		if(this.token != Token.T_EOF)
			debugError("analyza", "CHYBA: neocekavany znak na vstupu!");
		
		return !error == true ? outBuffer : null;
	}
	
	
	/**Nacte dalsi token z lexikalniho analyzeru.
	 * @return		Token ktery byl nacten (a ulozen jako aktualni).
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
			debugMessage("Srovnani (" + tokenDesc.get(this.token) + "," + 
					tokenDesc.get(token) + ") souhlasi.");
			readNextToken();
		}
		else
		{
			debugError("analyza", "CHYBA: srovnani (" + 
					tokenDesc.get(this.token) + "," +
					tokenDesc.get(token) + ") nesouhlasi!");
		}
	}
	
	
	/* -- prekladove funkce -- */
	
	
	/**Zapise instrukci do outBufferu. */
	private void printInstruction(String instruction)
	{
		outBuffer += instruction + "\n";
	}
	
	
	private void neg(DataType type) /* ineg, dneg */
	{
		switch(type)
		{
		case TYPE_INTEGER:
			printInstruction("ineg");
			break;
		case TYPE_REAL:
			printInstruction("dneg");
			break;
		default:
			debugError("preklad", "CHYBA: Neocekavany typ pro `neg'!");
		}
	}
	
	
	private DataType op(DataType left, DataType right, Token operator)
	{
		DataType resultType = DataType.TYPE_INTEGER;
		
		if(left != right)
		{
			if(right == DataType.TYPE_INTEGER)
				printInstruction("i2d");
			else
			{
				debugError("preklad", "CHYBA: Na prave strane operace nemuze byt REAL!");
			}
		}
		
		switch(operator)
		{
		case T_ADD:	 /* iadd, dadd */
			if(left == DataType.TYPE_INTEGER)
			{
				printInstruction("iadd");
				resultType = DataType.TYPE_INTEGER;
			}
			if(left == DataType.TYPE_REAL)
			{
				printInstruction("dadd");
				resultType = DataType.TYPE_REAL;
			}
			break;
		
		case T_SUB: /* isub, dsub */	
			if(left == DataType.TYPE_INTEGER)
			{
				printInstruction("isub");
				resultType = DataType.TYPE_INTEGER;
			}
			if(left == DataType.TYPE_REAL)
			{
				printInstruction("dsub");
				resultType = DataType.TYPE_REAL;
			}
			break;
			
		case T_MUL: /* imul, dmul */
			if(left == DataType.TYPE_INTEGER && right == DataType.TYPE_INTEGER)
			{
				printInstruction("imul");
				resultType = DataType.TYPE_INTEGER;
			}
			else
			{
				printInstruction("dmul");
				resultType = DataType.TYPE_REAL;				
			}
			break;
			
		case T_KW_DIV: /* idiv */
			printInstruction("idiv");
			resultType = DataType.TYPE_INTEGER;
			break;
			
		case T_DIV: /* ddiv */
			printInstruction("ddiv");
			resultType = DataType.TYPE_REAL;
			break;
			
		case T_KW_MOD: /* irem */
			printInstruction("irem");
			resultType = DataType.TYPE_INTEGER;
			break;
			
		default:
			debugError("preklad", "CHYBA: neocekavany operator pro `op'!");
		}
		
		return resultType;
	}
	
	
	/* FIXME: Lexer by mel value vracet jako nejaky set typu + hodnoty ... */
	private void ldc(DataType type, String value) /* ldc, ldc_w */
	{
		switch(type)
		{
		case TYPE_INTEGER:
			/* bipush ommited */
			int val = Integer.parseInt(value);
			if(val >= -128 && val <= 127)
				printInstruction("bipush " + value);
			else
				printInstruction("ldc " + value);
			break;
		case TYPE_REAL:
			printInstruction("ldc2_w " + value);
			break;
		default:
			debugError("preklad", "CHYBA: Neocekavany typ pro `ldc'!");
		}		
	}
	
	
	private void print(DataType type) /* invokevirtual pro vystup */
	{
		switch(type)
		{
		case TYPE_INTEGER:
			printInstruction("invokevirtual java/io/PrintStream/println(I)V");
			break;
		case TYPE_REAL:
			printInstruction("invokevirtual java/io/PrintStream/println(D)V");
			break;
		default:
			debugError("preklad", "CHYBA: Neocekavany typ pro `print'!");
		}
	}
		
	
	/* -- konec prekladovych funkci -- */
	/* -- zacatek expanznich funkci -- */
	
	
	private void Program()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_PROGRAM:
			debugMessage("1 Program -> program identifikator ; Blok .");
			compare(Token.T_KW_PROGRAM);
			
			printInstruction(".class public " + lexicalAnalyzer.getTokenValue());
			printInstruction(".super java/lang/Object");
			printInstruction(".method public static main([Ljava/lang/String;)V");
			printInstruction(".limit stack 20");
			printInstruction(".limit locals 20");
			compare(Token.T_IDENTIFIER);
			
			compare(Token.T_SEMICOLON);
			
			Blok();
			
			compare(Token.T_DOT);
			
			printInstruction("return");
			printInstruction(".end method");
						
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Program!");
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
			SlozenyPrikaz(0);
			break;
		case T_KW_VAR:
			/* 2 UsekDeklaraciPromennych SlozenyPrikaz */
			UsekDeklaraciPromennych();
			SlozenyPrikaz(0);
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Blok!");
		}
	}	
	
	
	private void UsekDeklaraciPromennych()
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_VAR:
			debugMessage("3 UsekDeklaraciPromennch -> var DeklaracePromennych ; ZbytekDeklaraciPromennych");
			compare(Token.T_KW_VAR);
			DeklaracePromennych();
			compare(Token.T_SEMICOLON);
			ZbytekDeklaraciPromennych();
			
			Iterator<String> varIdents = vars.keySet().iterator();
			while(varIdents.hasNext())
			{
				Variable var = vars.get(varIdents.next());
				switch(var.getType())
				{
				case TYPE_INTEGER:
					ldc(DataType.TYPE_INTEGER, "0");
					printInstruction("istore " + Integer.toString(var.getAddress()));
					break;
				case TYPE_REAL:
					ldc(DataType.TYPE_REAL, "0.0");
					printInstruction("dstore " + Integer.toString(var.getAddress()));
					break;					
				}
			}
			
			break;		
		case T_KW_BEGIN:
			debugMessage("4 UsekDeklaraciPromennch -> e");
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat UsekDeklaraciPromennych!");
		}
	}	
	
	
	private void DeklaracePromennych()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			debugMessage("5 DeklaracePromennch -> SeznamIdentifikatoru : OznaceniTypu");
			SeznamIdentifikatoru();
			compare(Token.T_COLON);
			
			DataType typ = OznaceniTypu();
			
			Iterator<String> varIdents = vars.keySet().iterator();
			while(varIdents.hasNext())
			{
				String ident = varIdents.next();
				
				Variable var = vars.get(ident);
				/* FIXME: tohle je trochu weird */
				if(!var.isSet())
				{	
					var.setAddress(lastAddr);
					var.setType(typ);
					
					lastAddr++;
					if(typ == DataType.TYPE_REAL)
						lastAddr++;
				}
			}			
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat DeklaracePromennych!");
		}
	}
	
	
	private void SeznamIdentifikatoru()
	throws IOException
	{
		switch(this.token)
		{	
		case T_IDENTIFIER:
			debugMessage("6 SeznamIdentifikatoru -> identifikator ZbytekSeznamuIdentifikatoru");
			
			addVar(lexicalAnalyzer.getTokenValue());
			compare(Token.T_IDENTIFIER);
			
			ZbytekSeznamuIdentifikatoru();
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat SeznamIdentifikatoru!");
		}
	}		

	
	private void ZbytekSeznamuIdentifikatoru()
	throws IOException
	{
		switch(this.token)
		{
		case T_COMMA:
			debugMessage("7 ZbytekSeznamuIdentifikatoru -> , SeznamIdentifikatoru");
			compare(Token.T_COMMA);
			SeznamIdentifikatoru();
			break;
		case T_COLON:
			debugMessage("8 ZbytekSeznamuIdentifikatoru -> e");
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat SeznamIdentifikatoru!");
		}
	}	
	
	
	private DataType OznaceniTypu()
	throws IOException
	{
		DataType resultType = null;
		
		switch(this.token)
		{
		case T_KW_INTEGER:
			debugMessage("9 OznaceniTypu -> integer");
			compare(Token.T_KW_INTEGER);
			resultType = DataType.TYPE_INTEGER;
			break;		
		case T_KW_REAL:
			debugMessage("10 OznaceniTypu -> real");
			compare(Token.T_KW_REAL);
			resultType = DataType.TYPE_REAL;
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat OznaceniTypu!");
		}
		
		return resultType;
	}
	
	
	private void ZbytekDeklaraciPromennych()
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			debugMessage("11 ZbytekDeklaraciPromennych -> DeklaracePromennych ; ZbytekDeklaraciPromennych");
			DeklaracePromennych();
			compare(Token.T_SEMICOLON);
			ZbytekDeklaraciPromennych();
			break;				
		case T_KW_BEGIN:
			debugMessage("12 ZbytekDeklaraciPromennych -> e");
			break;		
		default:
			debugError("analyza", "CHYBA: Nelze expandovat ZbytekDeklaraciPromennych!");
		}		
	}
	
	
	private int SlozenyPrikaz(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_KW_BEGIN:
			debugMessage("13 SlozenyPrikaz -> begin PosloupnostPrikazu end");
			compare(Token.T_KW_BEGIN);
			nextCmd = PosloupnostPrikazu(firstCmd);
			compare(Token.T_KW_END);
			break;		
		default:
			debugError("analyza", "CHYBA: Nelze expandovat SlozenyPrikaz!");
		}
		
		return nextCmd;
	}
	
	
	private int PosloupnostPrikazu(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
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
			debugMessage("14 PosloupnostPrikazu -> Prikaz ZbytekPosloupnostiPrikazu");
			int tmpNextCmd = Prikaz(firstCmd);
			nextCmd = ZbytekPosloupnostiPrikazu(tmpNextCmd);
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PosloupnostPrikazu!");
		}
		
		return nextCmd;
	}		
	
	
	private int Prikaz(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_IDENTIFIER:
			debugMessage("15 Prikaz -> PrirazovaciPrikaz");
			nextCmd = PrirazovaciPrikaz(firstCmd);
			break;
		case T_KW_BEGIN:
			debugMessage("16 Prikaz -> SlozenyPrikaz");
			nextCmd = SlozenyPrikaz(firstCmd);
			break;			
		case T_KW_IF:
			debugMessage("17 Prikaz -> PrikazIf");
			nextCmd = PrikazIf(firstCmd);
			break;		
		case T_KW_WHILE:
			debugMessage("18 Prikaz -> PrikazWhile");
			nextCmd = PrikazWhile(firstCmd);
			break;
		case T_KW_FOR:
			debugMessage("19 Prikaz -> PrikazFor");
			nextCmd = PrikazFor(firstCmd);
			break;				
		case T_KW_WRITELN:
			debugMessage("20 Prikaz -> PrikazWriteln");
			nextCmd = PrikazWriteln(firstCmd);
			break;		
		case T_SEMICOLON:
		case T_KW_ELSE:
		case T_KW_END:
			debugMessage("21 Prikaz -> PrazdnyPrikaz");
			nextCmd = PrazdnyPrikaz(firstCmd);
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Prikaz!");
		}
		
		return nextCmd;
	}	
	
	
	private int PrazdnyPrikaz(int firstCmd)
	throws IOException
	{	
		switch(this.token)
		{
		case T_SEMICOLON:	
		case T_KW_ELSE:
		case T_KW_END:
			debugMessage("22 PrazdnyPrikaz -> e");
			break;		
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PrazdnyPrikaz!");
		}

		return firstCmd;
	}	
	
	
	private int PrirazovaciPrikaz(int firstCmd)
	throws IOException
	{
		switch(this.token)
		{
		case T_IDENTIFIER:
			debugMessage("23 PrirazovaciPrikaz -> identifikator := Prikaz");
			
			String ident = lexicalAnalyzer.getTokenValue();
			Variable var = vars.get(ident);
			
			//System.err.println(ident +  "@" + var.getAddress() + ":" + var.getType().toString());
			
			if(var == null)
				debugError("preklad", "CHYBA: neznama promenna `" + lexicalAnalyzer.getTokenValue() + "'!");
			compare(Token.T_IDENTIFIER);
			compare(Token.T_ASSIGN);
			
			DataType type = Vyraz();
			
			if(type == var.getType())
				switch(type)
				{
				case TYPE_INTEGER:
					printInstruction("istore " + Integer.toString(var.getAddress()));
					break;
				case TYPE_REAL:
					printInstruction("dstore " + Integer.toString(var.getAddress()));
					break;					
				}
			else
				if(type == DataType.TYPE_INTEGER)
				{
					printInstruction("i2d");
					printInstruction("dstore " + Integer.toString(var.getAddress()));
				}
				else
					debugError("preklad", "CHYBA: nelze priradit REAL do promenne typu INTEGER!");
			break;		
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PrirazovaciPrikaz!");
		}
		
		return firstCmd;
	}		
	
	
	private DataType Vyraz()
	throws IOException
	{
		DataType resultType = null;
		
		switch(this.token)
		{		
		case T_OBRACKET:
		case T_ADD:
		case T_SUB:
		case T_INTEGER:
		case T_REAL:
		case T_IDENTIFIER:
			debugMessage("24 Vyraz -> Znamenko Term ZbytekVyrazu");
			
			Token sign = Znamenko();
			DataType type = Term();
			
			if(sign == Token.T_SUB)
				neg(type);
			
			resultType = ZbytekVyrazu(type);
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Vyraz!");
		}
		
		return resultType;
	}		
	
	
	private Token Znamenko()
	throws IOException
	{
		Token resultSign = null;
		
		switch(this.token)
		{
		case T_ADD:
			debugMessage("25 Znamenko -> +");
			compare(Token.T_ADD);
			
			resultSign = Token.T_ADD;
			break;
		case T_SUB:
			debugMessage("26 Znamenko -> -");
			compare(Token.T_SUB);
			
			resultSign = Token.T_SUB;
			break;			
		case T_INTEGER:
		case T_REAL:
		case T_IDENTIFIER:
		case T_OBRACKET:
			debugMessage("27 Znamenko -> e");
			
			resultSign = Token.T_ADD;
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Znamenko!");
		}
		
		return resultSign;
	}	
	
	
	private DataType Term()
	throws IOException
	{
		DataType resultType = null;
		
		switch(this.token)
		{
		case T_IDENTIFIER:
		case T_INTEGER:
		case T_REAL:	
		case T_OBRACKET:
			debugMessage("28 Term -> Faktor ZbytekTermu");
			
			DataType tmpType = Faktor();
			resultType = ZbytekTermu(tmpType);
			
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Term!");
		}
		
		return resultType;
	}	
	
	
	private DataType Faktor()
	throws IOException
	{
		DataType resultType = DataType.TYPE_INTEGER;
		
		switch(this.token)
		{
		case T_IDENTIFIER:
			debugMessage("29 Faktor -> identifikator");
			
			Variable var = vars.get(lexicalAnalyzer.getTokenValue());
			compare(Token.T_IDENTIFIER);
			
			resultType = var.getType();
			
			switch(resultType)
			{
			case TYPE_INTEGER:
				printInstruction("iload " + Integer.toString(var.getAddress()));
				break;
			case TYPE_REAL:
				printInstruction("dload " + Integer.toString(var.getAddress()));
				break;				
			}
			
			break;
		case T_INTEGER:
			debugMessage("30 Faktor -> celeCislo");
			
			ldc(DataType.TYPE_INTEGER, lexicalAnalyzer.getTokenValue());
			compare(Token.T_INTEGER);
			
			resultType = DataType.TYPE_INTEGER;
			
			break;
		case T_REAL:
			debugMessage("31 Faktor -> realneCislo");
			
			ldc(DataType.TYPE_REAL, lexicalAnalyzer.getTokenValue());
			compare(Token.T_REAL);
			
			resultType = DataType.TYPE_REAL;			
			
			break;			
		case T_OBRACKET:
			debugMessage("32 Faktor -> ( Vyraz )");
			compare(Token.T_OBRACKET);
			
			resultType = Vyraz();
			
			compare(Token.T_CBRACKET);
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Faktor!");
		}
		
		return resultType;
	}		
	
	
	private DataType ZbytekTermu(DataType left)
	throws IOException
	{
		DataType resultType = null;
		
		switch(this.token)
		{
		case T_DIV:
		case T_MUL:
		case T_KW_DIV:
		case T_KW_MOD:
			debugMessage("33 ZbytekTermu -> MultiplikativniOperator Faktor ZbytekTermu");
			Token operator = MultiplikativniOperator();
			
			if(left == DataType.TYPE_INTEGER && operator == Token.T_DIV)
			{
				printInstruction("i2d");
				left = DataType.TYPE_REAL;
			}
			
			DataType right = Faktor();			
			DataType tmpType = op(left, right, operator);
			
			resultType = ZbytekTermu(tmpType);
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
			debugMessage("34 ZbytekTermu -> e");
			
			resultType = left;
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat ZbytekTermu!");
		}
		
		return resultType;
	}		
	
	
	private Token MultiplikativniOperator()
	throws IOException
	{
		Token resultOperator = null;
		
		switch(this.token)
		{
		case T_MUL:
			debugMessage("35 MultiplikativniOperator -> *");
			compare(Token.T_MUL);
			resultOperator = Token.T_MUL;
			break;
		case T_DIV:
			debugMessage("36 MultiplikativniOperator -> /");
			compare(Token.T_DIV);
			resultOperator = Token.T_DIV;
			break;
		case T_KW_DIV:
			debugMessage("37 MultiplikativniOperator -> div");
			compare(Token.T_KW_DIV);
			resultOperator = Token.T_KW_DIV;
			break;
		case T_KW_MOD:
			debugMessage("38 MultiplikativniOperator -> mod");
			compare(Token.T_KW_MOD);
			resultOperator = Token.T_KW_MOD;
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat MultiplikativniOperator!");
		}
		
		return resultOperator;
	}			
	

	private DataType ZbytekVyrazu(DataType left)
	throws IOException
	{
		DataType resultType = null;
		
		switch(this.token)
		{
		case T_ADD:
		case T_SUB:
			debugMessage("39 ZbytekVyrazu -> AditivniOperator Term ZbytekVyrazu");
			
			Token operator = AditivniOperator();
			
			DataType right = Term();
			
			DataType tmpType = op(left, right, operator);
			
			resultType = ZbytekVyrazu(tmpType);
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
			debugMessage("40 ZbytekVyrazu -> e");
			
			resultType = left;
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat ZbytekVyrazu!");
		}
		
		return resultType;
	}	
	
	
	private Token AditivniOperator()
	throws IOException
	{
		Token resultOper = null;
		
		switch(this.token)
		{
		case T_ADD:
			debugMessage("41 AdditivniOperator -> +");			
			compare(Token.T_ADD);
			resultOper =Token.T_ADD;
			break;
		case T_SUB:
			debugMessage("42 AdditivniOperator -> -");			
			compare(Token.T_SUB);
			resultOper = Token.T_SUB;
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat AdditivniOperator!");
		}
		
		return resultOper;
	}
	

	private int ZbytekPosloupnostiPrikazu(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_SEMICOLON:
			debugMessage("43 ZbytekPosloupnostiPrikazu -> ; Prikaz ZbytekPosloupnostiPrikazu");
			compare(Token.T_SEMICOLON);
			
			int tmpCmd = Prikaz(firstCmd);
			nextCmd = ZbytekPosloupnostiPrikazu(tmpCmd);
			
			break;
		case T_KW_END:
			debugMessage("44 ZbytekPosloupnostiPrikazu -> e");
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat ZbytekPosloupnostiPrikazu!");
		}
		
		return nextCmd;
	}			
	
	
	private int PrikazIf(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_KW_IF:
			debugMessage("45 PrikazIf -> if Podminka then Prikaz CastElse");
			compare(Token.T_KW_IF);
			
			ParamNode ifNode = Podminka();
			String construct;
			String suffix = "";
			
			if(ifNode.type == DataType.TYPE_REAL)
			{
				construct = "if";
				printInstruction("dcmpg");
			}
			else
				construct = "if_icmp";
			
			switch(ifNode.oper)
			{
			case T_EQUAL:
				suffix = "ne";
				break;
			case T_DIFFER:
				suffix = "eq";
				break;
			case T_LESS:
				suffix = "ge";
				break;
			case T_GREAT:
				suffix = "le";
				break;
			case T_LESSEQ:
				suffix = "gt";
				break;
			case T_GREATEQ:
				suffix = "lt";
				break;
			}
			
			printInstruction(construct + suffix + " LABELIF" + Integer.toString(firstCmd));
			
			compare(Token.T_KW_THEN);
			
			int tmpNextCmd = Prikaz(firstCmd + 1);
			nextCmd = CastElse(tmpNextCmd, firstCmd);
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PrikazIf!");
		}
		
		return nextCmd;
	}		
	

	private ParamNode Podminka()
	throws IOException
	{
		ParamNode resultNode = null;
		
		switch(this.token)
		{
		case T_OBRACKET:	
		case T_ADD:	
		case T_SUB:	
		case T_INTEGER:	
		case T_REAL:	
		case T_IDENTIFIER:
			debugMessage("46 Podminka -> Vyraz RelacniOperator Vyraz");
			DataType left = Vyraz();
			Token oper = RelacniOperator();
			DataType right = Vyraz();
			
			resultNode = new ParamNode();
			resultNode.oper = oper;
			
			if(left == DataType.TYPE_INTEGER && right == DataType.TYPE_INTEGER)
				resultNode.type = DataType.TYPE_INTEGER;
			else if(left == DataType.TYPE_REAL && right == DataType.TYPE_REAL)
				resultNode.type = DataType.TYPE_REAL;
			else if(left == DataType.TYPE_REAL && right == DataType.TYPE_INTEGER)
			{
				resultNode.type = DataType.TYPE_REAL;
				printInstruction("i2d");
			}
			else
			{
				debugError("preklad", "CHYBA: Na prave strane podminky nemuze byt REAL!");
			}
			
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat Podminka!");
		}
		
		return resultNode;
	}	

	
	private Token RelacniOperator()
	throws IOException
	{
		Token resultOper = null;
		
		switch(this.token)
		{
		case T_LESS:
			debugMessage("49 RelacniOperator -> <");
			compare(Token.T_LESS);
			resultOper = Token.T_LESS;
			break;	
		case T_LESSEQ:
			debugMessage("51 RelacniOperator -> <=");
			compare(Token.T_LESSEQ);
			resultOper = Token.T_LESSEQ;
			break;
		case T_DIFFER:
			debugMessage("48 RelacniOperator -> <>");
			compare(Token.T_DIFFER);
			resultOper = Token.T_DIFFER;
			break;
		case T_EQUAL:
			debugMessage("47 RelacniOperator -> =");
			compare(Token.T_EQUAL);
			resultOper = Token.T_EQUAL;
			break;
		case T_GREAT:
			debugMessage("50 RelacniOperator -> >");
			compare(Token.T_GREAT);
			resultOper = Token.T_GREAT;
			break;
		case T_GREATEQ:
			debugMessage("50 RelacniOperator -> >=");
			compare(Token.T_GREATEQ);
			resultOper = Token.T_GREATEQ;
			break;				
		default:
			debugError("analyza", "CHYBA: Nelze expandovat RelacniOperator!");
		}
		
		return resultOper;
	}	
	
	
	private int CastElse(int firstCmd, int nestedCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_SEMICOLON:
			debugMessage("54 CastElse -> e");
			break;
		case T_KW_ELSE:
			debugMessage("53 CastElse -> else Prikaz");
			compare(Token.T_KW_ELSE);
			
			/* prelozit else */
			printInstruction("goto LABELELSE" + Integer.toString(firstCmd));
			printInstruction("LABELIF" + Integer.toString(nestedCmd) + ":");
			nextCmd = Prikaz(firstCmd + 1);
			/* preskocit else vetev */
			printInstruction("LABELELSE" + Integer.toString(firstCmd) + ":");
			/* konec else */
			
			//Prikaz();
			break;
		case T_KW_END:
			debugMessage("54 CastElse -> e");
			
			/* prelozit end */
			printInstruction("LABELIF" + Integer.toString(nestedCmd)+ ":");
			nextCmd = firstCmd;
			/* konec konec end */
			
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat CastElse!");
		}
		
		return nextCmd;
	}
	
	
	private int PrikazWhile(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_KW_WHILE:
			debugMessage("55 PrikazWhile -> while Podminka do Prikaz");
			compare(Token.T_KW_WHILE);
			
			printInstruction("LABELWHILEBEGIN" + Integer.toString(firstCmd)+ ":");
			
			ParamNode whileNode = Podminka();
			String construct;
			String suffix = "";
			
			if(whileNode.type == DataType.TYPE_REAL)
			{
				construct = "if";
				printInstruction("dcmpg");
			}
			else
				construct = "if_icmp";
			
			switch(whileNode.oper)
			{
			case T_EQUAL:
				suffix = "ne";
				break;
			case T_DIFFER:
				suffix = "eq";
				break;
			case T_LESS:
				suffix = "ge";
				break;
			case T_GREAT:
				suffix = "le";
				break;
			case T_LESSEQ:
				suffix = "gt";
				break;
			case T_GREATEQ:
				suffix = "lt";
				break;
			}
			
			printInstruction(construct + suffix + " LABELWHILEEND" + Integer.toString(firstCmd));			
			
			compare(Token.T_KW_DO);
	
			nextCmd = Prikaz(firstCmd + 1);
			
			printInstruction("goto LABELWHILEBEGIN" + Integer.toString(firstCmd));
			printInstruction("LABELWHILEEND" + Integer.toString(firstCmd) + ":");
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PrikazWhile!");
		}
		
		return nextCmd;
	}	
	
	
	private int PrikazFor(int firstCmd)
	throws IOException
	{
		int nextCmd = firstCmd;
		
		switch(this.token)
		{
		case T_KW_FOR:
			debugMessage("56 PrikazFor -> for identifikator := Vyraz CastFor Vyraz do Prikaz");
			compare(Token.T_KW_FOR);
			
			/* podminky pro validitu cyklu z hlediska ridici promenne */
			Variable var = vars.get(lexicalAnalyzer.getTokenValue());
			if(var == null)
				debugError("preklad", "CHYBA: neznama promenna `" + lexicalAnalyzer.getTokenValue() + "'!");
			if(var.getType() == DataType.TYPE_REAL)
				debugError("preklad", "CHYBA: ridici promenna cyklu nesmi byt REAL!");
			compare(Token.T_IDENTIFIER);
			
			compare(Token.T_ASSIGN);
			
			/* rizeni cyklu: pocatek */
			DataType beginType = Vyraz();
			if(beginType == DataType.TYPE_REAL)
				debugError("preklad", "CHYBA: ridici cast cyklu nesmi byt REAL!");			
			printInstruction("istore " + Integer.toString(var.getAddress()));
			
			printInstruction("LABELFORBEGIN" + Integer.toString(firstCmd) + ":");
			printInstruction("istore " + Integer.toString(var.getAddress()));
			
			/* rizeni cyklu: smer cyklu */
			CycleDirection dir = CastFor();
			
			/* rizeni cyklu: konec */
			DataType endType = Vyraz();
			if(endType == DataType.TYPE_REAL)
				debugError("preklad", "CHYBA: ridici cast cyklu nesmi byt REAL!");			
			
			compare(Token.T_KW_DO);
			
			switch(dir)
			{
			case CYCLE_TO:
				printInstruction("if_icmpgt LABELFOREND" + Integer.toString(firstCmd));
				break;
			case CYCLE_DOWNTO:
				printInstruction("if_icmplt LABELFOREND" + Integer.toString(firstCmd));
				break;
			}
			
			/* telo cyklu */
			nextCmd = Prikaz(firstCmd + 1);
			
			printInstruction("iinc " + Integer.toString(var.getAddress()) + (dir == CycleDirection.CYCLE_TO ? 1 : -1));
			printInstruction("goto " + "LABELFORBEGIN" + Integer.toString(firstCmd));
			printInstruction("LABELFOREND" + Integer.toString(firstCmd) + ":");
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PrikazFor!");
		}
		
		return nextCmd;
	}		
	
	
	private CycleDirection CastFor()
	throws IOException
	{
		CycleDirection resultDir = CycleDirection.CYCLE_TO;
		
		switch(this.token)
		{
		case T_KW_DOWNTO:
			debugMessage("58 CastFor -> downto");
			compare(Token.T_KW_DOWNTO);
			resultDir = CycleDirection.CYCLE_DOWNTO;
			break;
		case T_KW_TO:
			debugMessage("57 CastFor -> to");
			compare(Token.T_KW_TO);
			resultDir = CycleDirection.CYCLE_TO;			
			break;			
		default:
			debugError("analyza", "CHYBA: Nelze expandovat CastFor!");
		}
		
		return resultDir;
	}	
	
	
	private int PrikazWriteln(int firstCmd)
	throws IOException
	{
		switch(this.token)
		{
		case T_KW_WRITELN:
			debugMessage("59 PrikazWriteln -> writeln ( Vyraz )");
			compare(Token.T_KW_WRITELN);
			printInstruction("getstatic java/lang/System/out Ljava/io/PrintStream;");
			
			compare(Token.T_OBRACKET);
			DataType type = Vyraz();
			compare(Token.T_CBRACKET);
			
			print(type);
			//CHYBA z minuleho vydani 
			//Prikaz();
			break;
		default:
			debugError("analyza", "CHYBA: Nelze expandovat PrikazWriteln!");
		}
		
		return firstCmd;
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
		
		Compiler compiler = new Compiler(lexicalAnalyser);
		
		String out;
		if((out = compiler.compileSource()) != null)
			System.out.println(out);
	}
}


class Variable {
	private DataType type;
	private int address;
	private boolean set;
	
	public Variable()
	{
		this.set = false;
		this.address = -1;
		this.type = null;
	}
	
	public int getAddress()
	{
		return address;
	}
	
	public DataType getType()
	{
		return type;
	}
	
	public void setAddress(int address)
	{
		this.address = address;
		toggleSet();
	}
	
	public void setType(DataType type)
	{
		this.type = type;
		toggleSet();
	}
	
	public boolean isSet()
	{
		return this.set;
	}
	
	
	private void toggleSet()
	{
		if(this.address == -1 || this.type == null)
			this.set = false;
		else
			this.set = true;
	}
}


/* TODO: refactoring. sick helper */
class ParamNode {
	public DataType type;
	public Token oper;	
}


enum DataType {
	TYPE_INTEGER,
	TYPE_REAL
}

enum CycleDirection {
	CYCLE_TO,
	CYCLE_DOWNTO
}