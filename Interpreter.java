/*
   Minlu Jiang CISC3160
   Prof. Neng-Fa Zhou
   
   Description: (Working when variable are one digit now)
      The following defines a simple language, in which a program consists of assignments and 
   each variable is assumed to be of the integer type. For the sake of simplicity, only 
   operators that give integer values are included. Write an interpreter for the language 
   in a language of your choice. Your interpreter should be able to do the following for a 
   given program: (1) detect syntax errors; (2) report uninitialized variables; and (3) perform 
   the assignments if there is no error and print out the values of all the variables after all 
   the assignments are done.
   
   Sample inputs and outputs
      Input 1        Output 1
      x = 001;       error

      Input 2        Output 2
      x_2 = 0;       x_2 = 0

      Input 3        Output 3
      x = 0          error
      y = x;
      z = ---(x+y);

      Input 4                Output 4
      x = 1;                 x = 1
      y = 2;                 y = 2
      z = ---(x+y)*(x+-y);   z = 3

*/
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Interpreter{
   
   
   static ArrayList<String> list = new ArrayList<String>();
   //Store the variable name and its value
   static Hashtable<String, String> VariableTable = new Hashtable<String, String>();
   //Using vars ArrayList to print variable in order.
   static ArrayList<String> vars = new ArrayList<String>();
   //variable name pattern
   static Pattern var = Pattern.compile("([a-zA-Z$]|_[a-zA-Z0-9_$])([a-zA-Z0-9_$]*)?");
   //integer pattern
   static Pattern num = Pattern.compile("(-)?(0|([1-9][0-9]*?))");
   //open or close ()
   static Pattern openClose = Pattern.compile("(\\(|\\))*?");
   //open (
   static Pattern open = Pattern.compile("(\\()*?");
   //close )
   static Pattern close = Pattern.compile("(\\))*?");
   //valid math expression
   static Pattern exp = Pattern.compile(open + "(-)*?" + open + "(" + var + "|" + num + ")" 
   + close + "((\\+|-|\\*|/)(-)*?" + open + "("  + var + "|" + num + ")" + close + ")*?" + close);

   static Pattern toke = Pattern.compile("\\+|-|\\*|/|\\(|\\)|=");

   public static String run(String c){
      
      if(createList(c)){
         for(int i = 0; i < list.size(); i ++){
            //Checking if input is a simple assignment
            if(Pattern.matches(var + "=" + num, list.get(i))){
               updateTable(list.get(i)); 
            }
            //Checking if input is valid statement.
            else if(isValidStatement(list.get(i))){   
               vars.add(getVarName(list.get(i)));
               calculate(list.get(i));
            }
            //If it is no any valid statement.
            else{
               vars.clear();
               list.clear();
               VariableTable.clear();
               return "error\n";
            }
  
         }
      }
      else
         return "error\n";

      
      //print the result after code execute from Variable table
      if(!VariableTable.isEmpty()){
         /*VariableTable.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " = " + entry.getValue()); 
         });*/
         System.out.println();
         for(int i = 0; i < vars.size(); i ++){
            System.out.println(vars.get(i) + " = " + VariableTable.get(vars.get(i)));
         }
       
         vars.clear();
         list.clear();
         VariableTable.clear();
         return "";
      }
      
     vars.clear();
     list.clear();
     VariableTable.clear();
     return "error\n";

   }
   //Get variable name: before '=' 
   public static String getVarName(String s){
      String na = "";
      for(int i = 0; i < s.length(); i ++)
      {
         if(s.charAt(i) == '='){
            return na;
         }
         else{
            na += s.charAt(i);
         }
            
      
      }
      return "";
   }
   public static void calculate(String s){
      ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");
      boolean equalFlag = false; 
      String copy = "";
      int minus = 0;
      
      for(int i = 0; i < s.length(); i ++){
         if(s.charAt(i) == '='){
            equalFlag = true;
         }
         //After '=' check if variable in expression has value, replace with its value.
         else if(equalFlag && !Pattern.matches("" + toke, "" + s.charAt(i))){
            copy = VariableTable.get("" + s.charAt(i));
            s = s.replaceFirst("" + s.charAt(i), copy);
            
      
         }
         //if see two or more '-' in a row with current and next index are '-'
         //increase minus
         if(s.charAt(i) == '-' && s.charAt(i+1) == '-'){
            minus ++;
         }
         //if see the end of continuous '-', send to replace
         else if(s.charAt(i) == '-' && s.charAt(i+1) != '-') {
            if(minus > 0){
               s = replace(s, minus, i);
               minus = 0;
            }
         }
         
      }
      try {
         // Evaluate the expression
         Object result = engine.eval(s);
         if(Pattern.matches("" + num, String.valueOf(result))){
            VariableTable.put(vars.get(vars.size()-1), String.valueOf(result));
         }  
      }
      catch (ScriptException e) {
         // Something went wrong
         e.printStackTrace();
      }      
      
      //updateTable(s); 
   }
   public static String replace (String b, int m, int pos)
   {
      char[] ep = b.toCharArray(); 
      boolean minusFlag = false;
      //when two'-' m = 1, three'-', m = 2, ---- 3 
      //replace even number of '-', replace them with empty char
      if(m%2 == 1){
         for(int i = pos; i > pos - m - 1; i--){
            ep[i] = ' ';
         }
      }
      //odd number of '-' in sequence, replace first three of with '-1*' 
      //else replace with empty char
      else if(m%2 == 0){
         for(int i = pos - m; i < pos + 1; i++){
            if(!minusFlag){
               b = b.replaceFirst("---", "-1*");
               i +=2;
               minusFlag = true;
               ep = b.toCharArray(); 
            }
            else{
               ep[i] = ' ';
            }
         }
      }
      b = String.valueOf(ep);
      return b;
   }
   //Add to table for calculate and display
   public static void updateTable(String statement){
      String Vname = "";
      String n = "";
      boolean equalFlag = false;
     
      for(int i = 0; i < statement.length(); i ++){
        
         if(statement.charAt(i) == '=' ){
            equalFlag = true;
         }
         // before '=', variable name
         if(!equalFlag && statement.charAt(i)!= '='){
            Vname += statement.charAt(i);
         }
         // after '=', value
         else if(equalFlag && statement.charAt(i)!= '='){
            n += statement.charAt(i);
         }               
      }         
      vars.add(Vname);         
      VariableTable.put(Vname, n);
   }
   
   public static boolean isValidStatement(String s){
      return Pattern.matches(var + "=" + exp, s);
   }
   
   
   //Each element of ArrayList is one statement.
   public static boolean createList(String c){
      String s= "";
      //Stop the program when the last char of statement is not ;
      if(c.charAt(c.length()-1) != ';'){
         return false;
      }

      for(int i = 0; i < c.length(); i++){
         if(c.charAt(i) == ';'){
            //see one statement end with ';', add to the list
            list.add(s);
            s = "";
         }
         else if(c.charAt(i) != ' '){
            s += c.charAt(i);
         }
      }
      //Stop the program when there is no any compelete statement.(using ';' to check for now)
      if(list.size() == 0){
         return(false);
      }
      else{
         return(true);
      }
   }
   
   
   
   public static void main(String[]agrs){
      
      String code = "x = 001;";
      System.out.println("Input 1: " + code);
      System.out.print("Output 1: ");
      System.out.println(" " + run(code));
      
      code = "x_2 = 0;";
      System.out.println("Input 2: " + code);
      System.out.print("Output 2: ");
      System.out.println(" " + run(code));
      
      code = "x = 0 y = x; z = ---(x + y);";
      System.out.println("Input 3: " + code);
      System.out.print("Output 3: ");
      System.out.println(" " + run(code));
      
      code = "x = 1; y = 2; z = ---(x+y)*(x+-y);";
      System.out.println("Input 4: " + code);
      System.out.print("Output 4: ");
      System.out.println(" " + run(code));
   }
}