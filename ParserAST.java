import java.io.*;

public class ParserAST {

  protected MicroRLexer lexer;	// lexical analyzer
  protected Token token;	// current token

  public ParserAST () throws IOException {
    lexer = new MicroRLexer (new InputStreamReader (System . in));
    getToken ();
  }

  private void getToken () throws IOException { 
    token = lexer . nextToken (); 
  }

  // Program ::= source ( â€œList.Râ€ ) {FunctionDef} MainDef

  public Program program () throws IOException {
    Program prg;
    if (token . symbol () != Symbol . SOURCE) 	// source
      ErrorMessage . print (lexer . position (), "source EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LPAREN) 	// (
      ErrorMessage . print (lexer . position (), "( EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LISTR) 	// "List.R"
      ErrorMessage . print (lexer . position (), "\"List.R\" EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . RPAREN ) 	// )
      ErrorMessage . print (lexer . position (), ") EXPECTED");
    getToken ();
    while (token . symbol () == Symbol . ID) 	// {FunctionDef}
      prg = functionDef ();
    prg = mainDef ();                            	// MainDef
    if (token . symbol () != Symbol . EOF) 
      ErrorMessage . print (lexer . position (), "END OF PROGRAM EXPECTED");
    return prg;
  }

  // FunctionDef ::= 
  //     id <âˆ’ function ( [id {, id }] ) "{" {Statement} return ( Expr ) ; "}"

  public Program functionDef () throws IOException {
    Statement stmt = null;
    if (token . symbol () != Symbol . ID)        	// id
      ErrorMessage . print (lexer . position (), "id EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . ASSIGN)     	// <-
      ErrorMessage . print (lexer . position (), "<- EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . FUNCTION)  	// function
      ErrorMessage . print (lexer . position (), "function EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LPAREN)     	// (
      ErrorMessage . print (lexer . position (), "( EXPECTED");
    getToken ();
    if (token . symbol () == Symbol . ID) {     	// [ id
      getToken ();
      while (token . symbol () == Symbol . COMMA) {	// { ,
        getToken ();
        if (token . symbol () != Symbol . ID)      	// id
          ErrorMessage . print (lexer . position (), "id EXPECTED");
        getToken ();
      }                                          	// }
    }                                             	// ]
    if (token . symbol () != Symbol . RPAREN)   	// )
        ErrorMessage . print (lexer . position (), ") EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LBRACE)      	// "{"
      ErrorMessage . print (lexer . position (), "{ EXPECTED");
    getToken ();
    while (token . symbol () == Symbol . IF        	// { 
        || token . symbol () == Symbol . WHILE
        || token . symbol () == Symbol . ID
        || token . symbol () == Symbol . PRINT) {
      //statement ();     // Statement }
	  stmt = statementList();}
    if (token . symbol () != Symbol . RETURN)    	// return
      ErrorMessage . print (lexer . position (), "return EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LPAREN)    	// (
      ErrorMessage . print (lexer . position (), "( EXPECTED");
    getToken ();
    expr ();                                     	// Expr
    if (token . symbol () != Symbol . RPAREN)    	// )
      ErrorMessage . print (lexer . position (), ") EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . SEMICOLON)    	// ;
      ErrorMessage . print (lexer . position (), "; EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . RBRACE)    	// "}"
      ErrorMessage . print (lexer . position (), "} EXPECTED");
    getToken ();
	return stmt;
  }

  // MainDef ::= main < âˆ’ function ( ) "{" StatementList "}"

  public Program mainDef () throws IOException {
    if (token . symbol () != Symbol . MAIN)   	// main
      ErrorMessage . print (lexer . position (), "main EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . ASSIGN)  	// <-
      ErrorMessage . print (lexer . position (), "<- EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . FUNCTION) // function
      ErrorMessage . print (lexer . position (), "function EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LPAREN) 	// (
      ErrorMessage . print (lexer . position (), "( EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . RPAREN) 	// )
      ErrorMessage . print (lexer . position (), ") EXPECTED");
    getToken ();
    if (token . symbol () != Symbol . LBRACE) 	// "{"
      ErrorMessage . print (lexer . position (), "{ EXPECTED");
    getToken ();
    //statementList ();                      	// StatementList
	Statement stmt = statementList();
    if (token . symbol () != Symbol . RBRACE) 	// "}"
      ErrorMessage . print (lexer . position (), "} EXPECTED");
    getToken ();
    return stmt;
  }

  // StatementList ::= Statement { Statement }

  public Statement statementList () throws IOException {
    //statement ();                          	// Statement
	Statement stmt1 = null, stmt2;
    while (token . symbol () == Symbol . IF  	// {
        || token . symbol () == Symbol . WHILE
        || token . symbol () == Symbol . ID
        || token . symbol () == Symbol . PRINT) {
	  if (stmt1 == null)
		  stmt1 = statement();
	  else {
		  stmt2 = statement();
		  if (stmt2 != null)
			  stmt1 = new Statement (stmt1, stmt2);
	  }
	}
	return stmt1;
      //statement ();                          	// Statement }
  }

  // Statement ::= if ( Cond ) { StatementList } [else { StatementList }]
  //   | while ( Cond ) { StatementList }
  //   | id < âˆ’ Expr ;
  //   | print ( Expr ) ;

  public Statement statement () throws IOException {
	Expression exp;
	Statement stmt = null, stmt1, stmt2 = null;
	String id;
	VariableRef var;
	
    switch (token . symbol ()) {

      case IF :                                  	// if
        getToken ();
        if (token . symbol () != Symbol . LPAREN)   	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        exp = cond();                                 	// Cond
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . LBRACE) 	// "{"
          ErrorMessage . print (lexer . position (), "{ EXPECTED");
        getToken ();
		stmt1 = statement();
        //statementList ();                       	// StatementList
        if (token . symbol () != Symbol . RBRACE) 	// "}"
          ErrorMessage . print (lexer . position (), "} EXPECTED");
        getToken ();
        if (token . symbol () == Symbol . ELSE) {  	// [ else
          getToken ();
          if (token . symbol () != Symbol . LBRACE) 	// "{"
            ErrorMessage . print (lexer . position (), "{ EXPECTED");
          getToken ();
		  stmt2 = statement();
          //statementList ();                       	// StatementList
          if (token . symbol () != Symbol . RBRACE) 	// "}"
            ErrorMessage . print (lexer . position (), "} EXPECTED");
          getToken ();
        }// ]
		stmt = new Conditional (exp, stmt1, stmt2);
        break;

      case WHILE :                                  	// while
        getToken ();
        if (token . symbol () != Symbol . LPAREN)   	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        exp = cond();                                 	// Cond
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . LBRACE) 	// "{"
          ErrorMessage . print (lexer . position (), "{ EXPECTED");
        getToken ();
        stmt1 = statement();                       	// StatementList
        if (token . symbol () != Symbol . RBRACE) 	// "}"
          ErrorMessage . print (lexer . position (), "} EXPECTED");
        getToken ();
		stmt = new Loop (exp, stmt1);
        break;

      case ID :                                  	// id
	    id = token . lexeme();
        getToken ();
        if (token . symbol () != Symbol . ASSIGN)  	// <-
          ErrorMessage . print (lexer . position (), "<- EXPECTED");
		var = new VariableRef(id);
        getToken ();
        //expr ();                                 	// Expr
		exp = cond();
		stmt = new Assignment (var, exp);
        if (token . symbol () != Symbol . SEMICOLON)  	// ; 
          ErrorMessage . print (lexer . position (), "; EXPECTED");
        getToken ();
        break;

      case PRINT :                                  	// print
        getToken ();
        if (token . symbol () != Symbol . LPAREN)  	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        expr ();                                 	// Expr
        if (token . symbol () != Symbol . RPAREN) 	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . SEMICOLON)  	// ;
          ErrorMessage . print (lexer . position (), "; EXPECTED");
        getToken ();
        break;

      default :
        ErrorMessage . print (lexer . position (), "UNRECOGNIZABLE SYMBOL 241");

    }
    return stmt;
  }

  // Cond ::= AndExpr {|| AndExpr}

  public Expression cond () throws IOException {
	Expression exp;
    exp = andExpr ();                                	// AndExpr
    while (token . symbol () == Symbol . OR) { 	// { ||
      getToken ();
      //andExpr ();                              	// AndExpr }
	  exp = new Binary ("||", exp, andExpr());
    }
	return exp;
  }

  // AndExpr ::= RelExpr {&& RelExpr}

  public Expression andExpr () throws IOException {
	Expression conjunction;
    conjunction = relExpr ();                                	// relExpr
    while (token . symbol () == Symbol . AND) { // { &&
      getToken ();
      //relExpr ();                              	// relExpr }
	  conjunction = new Binary ("&&", conjunction, relExpr());
    }
	return conjunction;
  }

  // RelExpr ::= [!] Expr RelOper Expr

  public Expression relExpr () throws IOException {
	Expression equality;
	Expression relation = null;
        Expression expr = null;
	String op;
	equality = expr();
	relation = expr();
    if (token . symbol () == Symbol . NOT) 	// [!]
      getToken ();
    expr ();	// Expr
    switch (token . symbol ()) {
      case LT :                          	// <
	    op = token . lexeme ();
		getToken();
		relation = new Binary (op, relation, expr());
		expr = relation;
      case LE :                          	// <=
		op = token . lexeme ();
		getToken();
		relation = new Binary (op, relation, expr());
		expr = relation;
      case GT :                          	// >
		op = token . lexeme ();
		getToken();
		relation = new Binary (op, relation, expr());
		expr = relation;
      case GE :                         	// >=
		op = token . lexeme ();
		getToken();
		relation = new Binary (op, relation, expr());
		expr = relation;
      case EQ :                         	// ==
	    op = token . lexeme ();
		getToken();
		equality = new Binary (op, equality, expr());
		expr = equality;
      case NE :                         	// !=
        op = token . lexeme ();
		getToken();
		equality = new Binary (op, equality, expr());
		expr = equality;
		//getToken ();
        //expr ();                           	// Expr
	break;
      default : 
        ErrorMessage . print (lexer . position (), "UNRECOGNIZABLE SYMBOL 320");
        break;
    }
    return expr;
  }

  // Expr ::= MulExpr {AddOper MulExpr}
  // AddOper ::= + | âˆ’

  public Expression expr () throws IOException {
	Expression addition;
	String op;
	addition = mulExpr();
    //mulExpr ();                                 	// MulExpr
    while (token . symbol () == Symbol . PLUS   	// { +
	|| token . symbol () == Symbol . MINUS) {      	//   -
	  op = token . lexeme ();
      getToken ();
	  addition = new Binary (op, addition, mulExpr());
      //mulExpr ();                                 	// MulExpr }
    }
	return addition;
  }

  // MulExpr ::= PrefixExpr {MulOper PrefixExpr}
  // MulOper ::= * | /

  public Expression mulExpr () throws IOException {
	Expression term;
	String op;
	term = prefixExpr();
    //prefixExpr ();                               	// PrefixExpr
    while (token . symbol () == Symbol . TIMES  	// { *
        || token . symbol () == Symbol . SLASH) {  	//   /
	  op = token . lexeme ();
      getToken ();
      //prefixExpr ();                                	// PrefixExpr }
	  term = new Binary (op, term, prefixExpr());
    }
	return term;
  }

  // PrefixExpr ::= [AddOper] SimpleExpr
  // SimpleExpr ::= integer | ( Expr ) | as.integer ( readline ( ) ) 
  //   | id [ ( [Expr {, Expr}] ) ] | cons ( Expr , Expr ) | head ( Expr ) 
  //   | tail ( Expr ) | null ( )

  public Expression prefixExpr () throws IOException {
    if (token . symbol () == Symbol . PLUS     	// [ +
        || token . symbol () == Symbol . MINUS) //   - ]
      getToken ();

    Expression expr = null;

    switch (token . symbol ()) {

      case INTEGER :                             	// integer
        getToken ();
        break;

      case LPAREN :                                  	// (
        getToken ();
        expr ();                                	// Expr
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        break;

      case ASINTEGER :                            	// as.integer
        getToken ();
        if (token . symbol () != Symbol . LPAREN)  	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . READLINE) 	// readline
          ErrorMessage . print (lexer . position (), "readline EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . LPAREN)  	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        break;

      case ID :                                 	// id
        getToken ();
        if (token . symbol () == Symbol . LPAREN) { 	// [ (
          getToken ();
          if (token . symbol () != Symbol . RPAREN) { 	// [
            expr ();                            	// Expr
            while (token . symbol () == Symbol . COMMA) { // { ,
              getToken ();
              expr ();                           	// Expr }
            }                                    	// ]
            if (token . symbol () != Symbol . RPAREN) 	// )
              ErrorMessage . print (lexer . position (), ") EXPECTED");
          }                                      	// ]
          getToken ();
        }
        break;

      case CONS :                               	// cons
        getToken ();
        if (token . symbol () != Symbol . LPAREN)  	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        expr ();                                 	// Expr
        if (token . symbol () != Symbol . COMMA)   	// ,
          ErrorMessage . print (lexer . position (), ", EXPECTED");
        getToken ();
        expr ();                                 	// Expr
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        break;

      case HEAD :                               	// head
        getToken ();
        if (token . symbol () != Symbol . LPAREN) 	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        expr ();                                	// Expr
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        break;

      case TAIL :                                	// tail
        getToken ();
        if (token . symbol () != Symbol . LPAREN) 	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        expr ();                                	// Expr
        if (token . symbol () != Symbol . RPAREN)  	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        break;

      case NULL :                               	// null
        getToken ();
        if (token . symbol () != Symbol . LPAREN) 	// (
          ErrorMessage . print (lexer . position (), "( EXPECTED");
        getToken ();
        if (token . symbol () != Symbol . RPAREN) 	// )
          ErrorMessage . print (lexer . position (), ") EXPECTED");
        getToken ();
        break;

      default :
        ErrorMessage . print (lexer . position (), "UNRECOGNIZABLE SYMBOL 472");
    }
   return expr;
  }

}