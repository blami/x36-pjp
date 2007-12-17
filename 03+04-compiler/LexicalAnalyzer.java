/*
 * 	LexicalAnalyser.java
 *	Lexikalni analyzer pro Pascal.
 *
 *	Autor: Ondøej Baláž <ondra@blami.net>
 *	X36PJP, èt 14:30
 */
package x36pjp;
import java.io.*;
import java.util.*;


public class LexicalAnalyzer
{
	/* tabulka klicovych slov */
	private Hashtable<String, Token> keywords;
	
	/* vlastnosti tokenu */
	private String tok_val;
	private TokenType tok_type;
		
	private int i_chr;			/* vstupní znak */
	
	/* vstupní logika */
	private InputStreamReader is_r = null;
	private int line = 1;
	private int column = 1;
	
	
	/**Vytvori instanci <code>LexicalAnalyzer</code> nad existujicim <code>
	 * InputStream</code> ze ktereho cte zdrojovy text.
	 * @param is			<code>InputStream</code>, ze ktereho je cten 
	 * 						zdrojovy text.
	 * @throws IOException
	 */
	public LexicalAnalyzer(InputStream is)
	throws IOException
	{
		is_r = new InputStreamReader(is);
		keywords = new Hashtable<String, Token>();
		initKeywordsTable();
		read();
	}
	
	
	/**Metoda volana pred uvolnenim objektu z pameti. */
	protected void finalize() 
	throws IOException
	{
		if(is_r != null)
			is_r.close();
	}
	
	
	/**Inicializace tabulky klicovych slov pro vycleneni od identifikatoru. */
	private void initKeywordsTable()
	{
		keywords.clear();
		keywords.put("program", 	Token.T_KW_PROGRAM);
		keywords.put("var", 		Token.T_KW_VAR);
		keywords.put("integer", 	Token.T_KW_INTEGER);
		keywords.put("real", 		Token.T_KW_REAL);
		keywords.put("begin",	 	Token.T_KW_BEGIN);
		keywords.put("end", 		Token.T_KW_END);
		keywords.put("div",		 	Token.T_KW_DIV);
		keywords.put("mod",		 	Token.T_KW_MOD);
		keywords.put("and",		 	Token.T_KW_AND);
		keywords.put("or",		 	Token.T_KW_OR);
		keywords.put("if",		 	Token.T_KW_IF);
		keywords.put("then", 		Token.T_KW_THEN);
		keywords.put("else", 		Token.T_KW_ELSE);
		keywords.put("while", 		Token.T_KW_WHILE);
		keywords.put("do", 			Token.T_KW_DO);
		keywords.put("for",		 	Token.T_KW_FOR);
		keywords.put("to",		 	Token.T_KW_TO);
		keywords.put("downto", 		Token.T_KW_DOWNTO);
		keywords.put("write", 		Token.T_KW_WRITE);
		keywords.put("writeln",		Token.T_KW_WRITELN);
	}
	
	
	/**Cteci metoda. Precte pri kazdem zavolani ze vstupu prave jeden znak a
	 * ulozi do privatni tridni promenne <code>i_chr</code>. Zaroven pocita 
	 * radky a sloupce do promennych <code>line</code> a <code>column</code>.
	 * @throws IOException */
	private void read() 
	throws IOException 
	{
		int result = is_r.read();
		
		if(result != -1)
			column++;
		
		if(result == '\n')
		{
			column = 1;
			line++;
		}
		
		i_chr = result;
	}
	
	
	/**Preskoci vsechny mezery a nove radky na vstupu.
	 * @throws IOException */
	private void skipWhitespaces() 
	throws IOException
	{
		while (Character.isWhitespace(i_chr) || i_chr == '\n')
			read();		
	}
	
	
	/**Vraci nasledujici Token ve zdrojovem souboru. V pripade neuspechu funkce
	 * ukonci program (je to nesystemove a v realne praxi by to asi bylo 
	 * nahrazeno emitovanim nejake tomu vyhrazene vyjimky. 
	 * @return 			Token z vyctu "Token".
	 * @throws IOException */
	public Token nextToken() 
	throws IOException
	{		
		skipWhitespaces(); /* preskocime mezery */	

		/* preskocit komentar */
		if(i_chr == '{')
		{
			while(i_chr != '}')
			{
				if(i_chr == -1)
					error("Neocekavany konec souboru v komentari!", 
							line, column);
				read();
			}
			
			read();
			skipWhitespaces(); /* preskocime mezery za komentarem */
		}			
		
		/* eof */
		if(i_chr == -1)
		{
			tok_val = "";
			tok_type = TokenType.TYPE_NONE; 
			return Token.T_EOF;
		}
		
		/* integer nebo real */
		if(Character.isDigit(i_chr))
		{
			tok_val = "";
			tok_type = TokenType.TYPE_INTEGER;
			
			while(Character.isDigit(i_chr) || i_chr == '.')
			{
				tok_val += Character.toString((char)i_chr);
				if(i_chr == '.')
				{
					if(tok_type == TokenType.TYPE_INTEGER)
						/* prvni carka, dobra carka */
						tok_type = TokenType.TYPE_REAL;
					else
						/* opakovany vtip uz neni vtipem */
						error("Neocekavany znak '.'!", line, column);
				}
				read();
			}
			if(tok_type == TokenType.TYPE_INTEGER)
				return Token.T_INTEGER;
			else
				return Token.T_REAL;			
		}
		
		/* identifikator (zahrnuje klicova slova) */
		if(Character.isLetter(i_chr))
		{
			tok_val = "";
			tok_type = TokenType.TYPE_IDENTIFIER;
			
			while(Character.isLetterOrDigit(i_chr))
			{
				tok_val += Character.toString((char)i_chr);
				read();
			}
			
			/* podivat se do tabulky klicovych slov jazyka jestli jsme nenasli
			 * klicove slovo. Pokud ano, vratime jeho token. */
			Token retToken = keywords.get(tok_val.toLowerCase());
			
			/* u klicoveho slova nemusime znat hodnotu (ani vlastne nechceme) */
			if(retToken != null)
			{
				tok_type = TokenType.TYPE_NONE;
				tok_val = "";
			}
			
			return retToken == null ? Token.T_IDENTIFIER : retToken;
		}
		
		/* specialni symboly */
		tok_type = TokenType.TYPE_NONE;
		tok_val = "";
		
		switch(i_chr)
		{
		case '(':
			read();
			return Token.T_OBRACKET;
		
		case ')':
			read();
			return Token.T_CBRACKET;
		
		case ',':
			read();
			return Token.T_COMMA;
		
		case ':':
			read();
			if(i_chr == '=')
			{
				read();
				return Token.T_ASSIGN;
			}
			return Token.T_COLON;

		case ';':
			read();
			return Token.T_SEMICOLON;			
			
		case '+':
			read();
			return Token.T_ADD;
		
		case '-':
			read();
			return Token.T_SUB;
		
		case '*':
			read();
			return Token.T_MUL;
		
		case '/':
			read();
			return Token.T_DIV;
			
		case '=':
			read();
			return Token.T_EQUAL;
		
		case '<':
			read();
			if(i_chr == '>')
			{
				read();
				return Token.T_DIFFER;
			}
			if (i_chr == '=')
			{
				read();
				return Token.T_LESSEQ;
			}
			return Token.T_LESS;
		
		case '>':
			read();
			if (i_chr == '=')
			{
				read();
				return Token.T_GREATEQ;
			}
			return Token.T_GREAT;
		
		case '.':
			read();
			return Token.T_DOT;
		
		default:
			error("Neocekavany znak na vstupu: `" + 
					Character.toString((char)i_chr) + "'!", line, column);
			return Token.T_EOF; /* berlicka */
		}
	}
	
	
	/**Vrati hodnotu nalezeneho tokenu. Tato funkce zatim vraci jen String,
	 * protoze v tomto programu zatim vic nepotrebuju (ale myslim na lepsi 
	 * zitrky a vim typ hodnoty pomoci <code>tok_type</code>. Az budu delat
	 * dalsi casti prekladace, tuto funkci nahradim nejakou co bude zrejme 
	 * vracet presne to co potrebuji (integer, real a nebo retezec).
	 * @return				Hodnota nalezeneho tokenu (pokud nejaka je). */
	public String getTokenValue()
	{
		if(tok_type != TokenType.TYPE_NONE)
			return tok_val;
		else 
			return null;
	}
	
	
	/**Vypise chybu ve zdrojovem kodu na chybovy vystup a ukonci program.
	 * @param msg		Chybova hlaska.
	 * @param line		Radek, kde chyba nastala.
	 * @param column	Sloupek kde chyba nastala.
	 */
	static void error(String msg, int line, int column)
	{
		System.err.println("LEXIKALNI CHYBA (radek:" + line + ", sloupec: " + column + 
				")\n" + msg);
		System.exit(1);
	}
	
	
	/**Pomocna funkce ktera pripravi hesovaci tabulku elementu tak, aby bylo
	 * snadne pomoci klice (rozpoznaneho tokenu) vypsat o jaky token se jedna.
	 * Tato funkce je zde pro ucely unit-testu.
	 * @return			Hesovaci tabulka Token:String(popis/nazev tokenu). */ 
	static Hashtable<Token, String> buildElementsOutputTable()
	{
		Hashtable<Token, String> ret = new Hashtable<Token, String>();
		
		ret.put(Token.T_IDENTIFIER	, "IDENTIFIER");
		ret.put(Token.T_INTEGER		, "INTEGER");
		ret.put(Token.T_REAL		, "REAL");
		ret.put(Token.T_OBRACKET	, "OBRACKET");
		ret.put(Token.T_CBRACKET	, "CBRACKET");
		ret.put(Token.T_COMMA		, "COMMA");
		ret.put(Token.T_COLON		, "COLON");
		ret.put(Token.T_SEMICOLON	, "SEMICOLON");
		ret.put(Token.T_ASSIGN		, "ASSIGN");
		ret.put(Token.T_ADD			, "ADD");
		ret.put(Token.T_SUB			, "SUB");
		ret.put(Token.T_MUL			, "MUL");
		ret.put(Token.T_DIV			, "DIV");
		ret.put(Token.T_EQUAL		, "EQUAL");
		ret.put(Token.T_DIFFER		, "DIFFER");
		ret.put(Token.T_LESS		, "LESS");
		ret.put(Token.T_GREAT		, "GREAT");
		ret.put(Token.T_LESSEQ		, "LESSEQ");
		ret.put(Token.T_GREATEQ		, "GREATEQ");
		ret.put(Token.T_DOT			, "DOT");
		ret.put(Token.T_KW_PROGRAM	, "KEYWORD_program");
		ret.put(Token.T_KW_VAR		, "KEYWORD_var");
		ret.put(Token.T_KW_INTEGER	, "KEYWORD_integer");
		ret.put(Token.T_KW_REAL		, "KEYWORD_real");
		ret.put(Token.T_KW_BEGIN	, "KEYWORD_begin");
		ret.put(Token.T_KW_END		, "KEYWORD_end");
		ret.put(Token.T_KW_DIV		, "KEYWORD_div");
		ret.put(Token.T_KW_MOD		, "KEYWORD_mod");
		ret.put(Token.T_KW_AND		, "KEYWORD_and");
		ret.put(Token.T_KW_OR		, "KEYWORD_or");
		ret.put(Token.T_KW_IF		, "KEYWORD_if");
		ret.put(Token.T_KW_THEN		, "KEYWORD_then");
		ret.put(Token.T_KW_ELSE		, "KEYWORD_else");
		ret.put(Token.T_KW_WHILE	, "KEYWORD_while");
		ret.put(Token.T_KW_DO		, "KEYWORD_do");
		ret.put(Token.T_KW_FOR		, "KEYWORD_for");
		ret.put(Token.T_KW_TO		, "KEYWORD_to");		
		ret.put(Token.T_KW_DOWNTO	, "KEYWORD_downto");
		ret.put(Token.T_KW_WRITE	, "KEYWORD_write");
		ret.put(Token.T_KW_WRITELN	, "KEYWORD_writeln");
		
		return ret;
	}
	
	
	/**Vstupni bod do programu (self-test). 
	 * @param args		Pole argumentu predanych prikazovou radkou. V uvahu se 
	 * 					bere index <code>1</code> ktery obsahuje cestu ke 
	 * 					vstupnimu souboru.*/
	public static void main(String args[]) 
	throws IOException
	{
		Hashtable<Token, String> elements = buildElementsOutputTable();
		LexicalAnalyzer lexicalAnalyzer = null;
		
		try
		{
			if(args.length == 1)
				lexicalAnalyzer = 
					new LexicalAnalyzer(new FileInputStream(args[0]));
		}
		catch (FileNotFoundException e)
		{
			lexicalAnalyzer = null;
		}
		
		if(lexicalAnalyzer == null)
		{
			System.out.println("Vstup ze stdin:");
			lexicalAnalyzer = new LexicalAnalyzer(System.in);
		}
		else 
		{
			System.out.println("Vstup ze souboru " + args[0]);
		}
		
		Token token;
		while((token = lexicalAnalyzer.nextToken()) != Token.T_EOF)
		{
			String outToken = "<" + elements.get(token);
			
		    if (lexicalAnalyzer.getTokenValue() == null)
		    	outToken += ">";
		    else
		    	outToken += ": \"" + lexicalAnalyzer.getTokenValue() + "\">";
		    
			System.out.println(outToken);
		}
	}
}


/** Mnozina typu hodnot tokenu, pro tokeny, ktere nemaji hodnotu (typicky napr.
 *  operatory) se pouziva TYPE_NONE. */
enum TokenType {
	TYPE_INTEGER,
	TYPE_REAL,
	TYPE_IDENTIFIER,
	TYPE_NONE
}
