package org.mozilla.javascript.tests;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.drivers.TestUtils;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.ShellContextFactory;

import junit.framework.TestCase;

/**
 * @author Tejas Saoji
 */
public class TestTainting extends TestCase {

  protected final Global global = new Global();

  public TestTainting() {
    global.init(contextFactory);
  }

  @Override
  protected void setUp() {
    TestUtils.setGlobalContextFactory(contextFactory);
  }

  @Override
  protected void tearDown() {
    TestUtils.setGlobalContextFactory(null);
  }

  private ShellContextFactory contextFactory = new ShellContextFactory();

  //TAINT, UNTAINT, ISTAINTED functions
  public void testTainting01() {
      Object result = runScript(
    		  "var x = \"hey\"; isTainted(x);");
      assertEquals(false, result);
  }
  
  public void testTainting02() {
      Object result = runScript(
    		  "var x = \"hey\"; var y = taint(x); var z = untaint(x); isTainted(z)");
      assertEquals(false, result);
  }
  
  public void testTainting03() {
      Object result = runScript(
    		  "var x = \"hey\"; taint(x); var y = \"hello\"; taint(y); untaint(x); isTainted(x)");
      assertEquals(false, result);
  }
  
  public void testTainting04() {
      Object result = runScript(
    		  "var x = \"hey\"; var y = taint(x); var a = \"hello\"; var b = taint(a); var c = untaint(b); isTainted(y); ");
      assertEquals(true, result);
  }
  
  public void testTainting05() {
      Object result = runScript(
    		  "var x = \"hey\"; taint(x); x = \"hello\"; isTainted(x)");
      assertEquals(false, result);
  }
  
  //VAR Y = X, where X is tainted
  public void testTainting06() {
      Object result = runScript(
    		  "var x = \" hey \"; var y = taint(x); var z = y.trimLeft(); var a = z; print(y); isTainted(a);");
      assertEquals(true, result);
  }
  
  //CONCAT function, where either of the two strings or both the strings are tainted
  public void testTainting07() {
      Object result = runScript(
    		  "var x = \"Hello\"; var y = taint(x); var z = \"World\"; var a = y.concat(z); isTainted(a);");
      assertEquals(true, result);
  }
  
  //String concatenation using '+' operator, where one of the strings or all the strings are tainted
  public void testTainting08() {
      Object result = runScript(
    		  "var x = \"Hello\"; var y = taint(x); var z = \"World\"; var a = x + y + z; isTainted(a);");
      assertEquals(true, result);
  }
  
  public void testTainting09() {
      Object result = runScript(
    		  "var x = \"Hello\"; var y = \"World\"; var z = x + y + x; isTainted(z);");
      assertEquals(false, result);
  }
  
  // SUBSTRING SUBSRT and SLICE function, where the original string is tainted
  public void testTainting10() {
      Object result = runScript(
    		  "var x = \"Hello Tejas!\"; var y = x.substring(0,5); isTainted(y);");
      assertEquals(false, result);
  }
  
  public void testTainting11() {
      Object result = runScript(
    		  "var x = \"Hello Tejas!\"; var y = taint(x); var z = y.substring(0,5); isTainted(z);");
      assertEquals(true, result);
  }
  
//string TRIM, TRIMLEFT, and TRIMRIGHT functions
  public void testTainting12() {
      Object result = runScript(
    		  "var x = \"     Tejas     \"; var y = x.trim(); isTainted(y);");
      assertEquals(false, result);
  }
  
  public void testTainting13() {
      Object result = runScript(
    		  "var x = \"     Tejas     \"; var y = taint(x); var z = y.trim(); isTainted(z);");
      assertEquals(true, result);
  }
  
//string REPLACE function
  public void testTainting14() {
      Object result = runScript(
    		  "var x = \"Hello World\"; var y = x.replace(\"Hello\",\"Hey\"); isTainted(y);");
      assertEquals(false, result);
  }
    
  public void testTainting15() {
	  Object result = runScript(
    		  "var x = \"Hello World\"; var y = taint(x); var z = y.replace(\"Hello\",\"Hey\"); isTainted(z);");
      assertEquals(true, result);
  }
  
//string BOLD, ITALICS, FONTSIZE, FONTCOLOR, LINK, SUP, SUB, SMALL, BIG, STRIKE, etc. functions
  public void testTainting16() {
	  Object result = runScript(
    		  "var x = \"Hello World\"; var y = taint(x); var z = y.bold().italics().fontsize(6).fontcolor(\"red\").link(); isTainted(z);");
      assertEquals(true, result);
  }

  public void testTainting17() {
	  Object result = runScript(
    		  "var x = \"Hello World\"; taint(x); x = \" Hey! \"; var y = x.bold().italics().fontsize(6).fontcolor(\"red\").link(); isTainted(y);");
      assertEquals(false, result);
  }
  
//Does not work for toUpperCase and toLowerCase  CHECK ONCE!!!!
  
//global and local scope taint
  
  public void testTaintingGTC01() {
      Object result = runScript(
    		  "var x = \"  Hello  \"; var y = taint(x); var z = y.bold().italics().fontsize(6).fontcolor(\"yellow\").link(); var a = z; var b = a.trim(); "
    		  + "var c = b.concat(\" Tejas \"); var d = c.substr(0,3); isTainted(d);");
      assertEquals(true, result);
  }
  
  public void testTaintingGTC02() {
	  Object result = runScript(
    		  "var x = \"  Hello  \"; taint(x); x = \" Hello \"; var y = x; var z = y.trim(); var a = z.replace(\"Hello\",\"Hey\"); var b = a.concat(\" Tejas \"); var c = b.substr(0,3); isTainted(c);");
      assertEquals(false, result);
  }
  
  private Object runScript(final String scriptSourceText) {
	contextFactory.setOptimizationLevel(-1);
    return contextFactory.call(new ContextAction() {
      public Object run(Context context) {
        Script script = context.compileString(scriptSourceText, "", 1, null);
        return script.exec(context, global);
      }
    });
  }
}
