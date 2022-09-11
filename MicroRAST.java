
public class MicroRAST {

  public static void main (String args []) throws java.io.IOException {

    System . out . println ("Source Program");
    System . out . println ("--------------");
    System . out . println ();

    ParserAST microR = new ParserAST ();
	Program program = microR . program ();

    System . out . println ();
    System . out . println ();
	System . out . println ("Abstract Syntax Tree");
	System . out . println ("--------------------");
	System . out . println ();
    System . out . println (program);
  }

}
