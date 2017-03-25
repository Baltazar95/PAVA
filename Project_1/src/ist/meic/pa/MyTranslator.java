package ist.meic.pa;

import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtNewConstructor;
import javassist.NotFoundException;
import javassist.Translator;

public class MyTranslator implements Translator
{
	public Map<String, String> map = new HashMap<String, String>();
	public Map<String, String> convertTypes = new HashMap<String, String>();
	
	//In the start method of our translator we fill our map that contains all 
	//the primitive types that we are going to use to cast our object variable
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException 
	{
		convertTypes.put("int", "Integer");
		convertTypes.put("float", "Float");
		convertTypes.put("double", "Double");
		convertTypes.put("long", "Long");
		convertTypes.put("boolean", "Boolean");
		convertTypes.put("short", "Short");
		convertTypes.put("byte", "Byte");
		convertTypes.put("char", "Character");
	}
	
	//The onLoad method is used to catch the wanted class has it is being loaded.
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException 
	{
		CtClass ctClass = pool.get(className); 	
		try 
		{
			CheckKeywordArgs(ctClass);
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}
	
	//This method checks for the KeywordArgs annotation. 
	//Then it gets all the respective keywords associated with the passed class constructor.
	public void CheckKeywordArgs(CtClass ctClass) throws ClassNotFoundException, CannotCompileException, NotFoundException 
	{
		for(CtConstructor ctConstructor: ctClass.getDeclaredConstructors())
		{
			//This loop will enable the programmers to have other annotations because it only cares with the KeywordArgs annotation
			for(Object annotation: ctConstructor.getAnnotations())
			{
				if(annotation instanceof KeywordArgs)
				{
					SplitArgs(((KeywordArgs) annotation).value());
					CheckKeywordArgs_aux(ctClass.getSuperclass());
					Injection(ctClass, ctConstructor);
					//This line of code adds a default constructor that was suppose to be created by java.
					ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
					break;
				}
			}
		}
	}
	
	//This is an auxiliary method that is responsible for checking all the keywords in the super class constructor
	public void CheckKeywordArgs_aux(CtClass ctClass) throws ClassNotFoundException, CannotCompileException, NotFoundException 
	{
		for(CtConstructor ctConstructor: ctClass.getDeclaredConstructors())
		{	
			//This loop will enable the programmers to have other annotations because it only cares with the KeywordArgs annotation
			for(Object annotation: ctConstructor.getAnnotations())
			{
				if(annotation instanceof KeywordArgs)
				{
					SplitArgs(((KeywordArgs) annotation).value());
					//This is the stop condition for the recursive call of this method.
					if(!ctClass.getSuperclass().getName().equals("java.lang.Object"))
					{
						CheckKeywordArgs_aux(ctClass.getSuperclass());
					}
					break;
				}
			}
		}
	}
	
	//This method is responsible for the injection of the code in the caught class constructor.
	public void Injection(CtClass ctClass, CtConstructor ctConstructor) throws CannotCompileException, NotFoundException
	{
		String attributetype, attributeName;
		//These two variables are for special cases we want to catch
		//The verifyVariable its a boolean to check if there is a value for the attribute in the instantiation
		//The check vector is used to verify if all the passed parameters in the instantiation are actual attributes of the class
		String body="{ "
				+ "		boolean verifyVariable = false;"
				+ "	   	String[] check = new String[$1.length];";
		
		for(CtField ct : ctClass.getFields())
		{
			attributeName = ct.getName();
			if(map.containsKey(attributeName))
			{
				attributetype = ct.getType().getName();
				
				body = body + "		for(int i=0; i < $1.length; i++)"
							+ "		{"
							+ "			if($1[i].equals(\""+ attributeName +"\"))"
							+ "			{"
							+ "				check[i] = \"Here\";";

				//This condition checks the actual type we want to convert to
				if(attributetype.startsWith("java"))
				{
					String[] splitted = attributetype.split("\\.");
					body = body + attributeName  + "= (" + splitted[splitted.length-1] + ") $1[i+1];"
								+ "           		verifyVariable = true;"
								+ "           		i++;";
				}
				else
				{
					body = body + attributeName + "= ((" + convertTypes.get(attributetype)  
									+ ") $1[i+1])." + attributetype + "Value();"
								+ " verifyVariable = true;"
								+ " i++;"; 
				}
				
				body = body + "			check[i] = \"Here\";"
							+ "		}"
							+ "} "
							+ "if(!verifyVariable)"
							+ "{";
				
				if(!map.get(attributeName).equals(""))
				{
					body = body + " " + attributeName + "=" + map.get(attributeName) + ";";
				}
				
				body = body + "}"
							+ "else"
							+ "{"
							+ "     verifyVariable = false;"
							+ "}";
			}
		}
		
		body = body + "		for(int j = 0; j<$1.length; j++)"
					+ "		{"
					+ "			if(check[j] == null)"
					+ "			{"
					+ "				throw new RuntimeException(\"Unrecognized keyword: \" + $1[j]);"
					+ "			}"
					+ "		}"
					+ "}";
		
		ctConstructor.setBody(body);
		map.clear();
	}
	
	//This method is responsible for splitting the the keywords and their values and save them in a map.
	public void SplitArgs(String args)
	{
		String[] paramValue; 
		String[] parts = args.split(",");
		for(String param : parts)
		{
			paramValue = param.split("=");
			//This condition gives priority to the subclass keyword values and 
			//overrides one if it is the predefined value of java
			if(!map.containsKey(paramValue[0]) || map.get(paramValue[0]).equals(""))
			{
				if(paramValue.length == 1)
				{
					map.put(paramValue[0], "");
				}
				else
				{
					map.put(paramValue[0], paramValue[1]);
				}
			}
		}
	}
	
}