package ist.meic.pa;

import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.Translator;


public class MyTranslator implements Translator
{
	public Map<String, String> map = new HashMap<String, String>();
	public Map<String, String> convertTypes = new HashMap<String, String>();
	
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
		convertTypes.put("java.lang.String", "String");
	}
	
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException 
	{
		CtClass ctClass = pool.get(className); 	//pool - the ClassPool that this translator should use.
		//classname - the name of the class being loaded.
		try 
		{
			CheckKeywordArgs(ctClass);
		} 
		catch (ClassNotFoundException e) 
		{
			//FIXME DEAL WITH EXCEPTION
			e.printStackTrace();
		}
	}
	
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
					Injection(ctClass, ctConstructor, annotation);
					break;
				}
			}
		}
	}
	
	public void Injection(CtClass ctClass, CtConstructor ctConstructor, Object annotation) throws CannotCompileException, NotFoundException
	{
		String body="{ boolean verifyVariable = false;"
					+ "String t=\"\";";
		
		//FIXME continue this
		for(CtField ct : ctClass.getDeclaredFields())
		{
			if(map.containsKey(ct.getName()))
			{
//				body = body + "System.out.println(\""+ct.getName()+"\");\n";
				//System.out.println(ct.getName());	
				
				body = body + "for(int i=0; i < $1.length; i++)\n"
							+ "{\n"
							+ "    if($1[i].equals(\""+ ct.getName() +"\"))\n"
							+ "    {\n"	
							+ "        if(" + convertTypes.get(ct.getType().getName())  + ".class.isInstance($1[i+1]))\n"
							+ "        {\n"
							+ "				t = \"" + ct.getType().getName() + "\";" 
							+ "	       	  if(t.equals(\"java.lang.String\"))\n"
							+ "			  {\n"
							+ "					System.out.println(($1[i+1]));\n"
							+ "					"+ct.getName()+"=((String) $1[i+1]);\n"
							+ "           		verifyVariable = true;\n"
							+ "           		i++;\n"
							+ "        	  }\n"
							+ "			  else\n"
							+ "			  {\n"
							+ "					System.out.println(($1[i+1]));\n"
							+ "           		"+ ct.getName() + "= ((" + convertTypes.get(ct.getType().getName())  + ") $1[i+1])." + ct.getType().getName() + "Value();\n"
							+ "           		verifyVariable = true;\n"
							+ "           		i++;\n"
							+ "			  }\n"
							+ "		   }\n"
							+ "        else\n"
							+ "        {\n"
							+ "           System.out.println(\"Wrong value for variable "+ ct.getName() +"\");\n"
							+ "        }\n"
							+ "     }\n"
							+ "}\n"
							+ "if(!verifyVariable)\n"
							+ "{\n"
							+ " " + ct.getName() + "=" + map.get(ct.getName()) + ";\n"
							+ "}\n"
							+ "else\n"
							+ "{\n"
							+ "     verifyVariable = false;\n"
							+ "}\n";
							//+ "System.out.println("+body+");\n"; 
				
			}
			else
			{
				//FIXME create exception
			}
		}
		body = body + "}";
		
		//System.out.println(body);
		
		ctConstructor.setBody(body);
		
	}
	
	public void SplitArgs(String args)
	{
		String[] paramValue; 
		String[] parts = args.split(",");
		for(String param : parts)
		{
			paramValue = param.split("=");
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
	
	
	
	//FIXME APAGAR!!!!!!!!!
	public void printshit()
	{
		for(String key : map.keySet())
		{
			System.out.println(key);
			System.out.println(map.get(key));
		}
	}
}
