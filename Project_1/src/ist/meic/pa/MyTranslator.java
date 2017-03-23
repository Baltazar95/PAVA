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
					CheckKeywordArgs_aux(ctClass.getSuperclass());
					Injection(ctClass, ctConstructor);
					break;
				}
			}
		}
	}
	
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
					if(!ctClass.getSuperclass().getName().equals("java.lang.Object"))
						CheckKeywordArgs_aux(ctClass.getSuperclass());
					ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
					break;
				}
			}
		}
	}
	
	public void Injection(CtClass ctClass, CtConstructor ctConstructor) throws CannotCompileException, NotFoundException
	{
		String body="{ boolean verifyVariable = false;"
				+ "	   String[] check = new String[$1.length-1];";
		String[] fields;
		//FIXME continue this
		for(CtField ct : ctClass.getFields())
		{
			if(map.containsKey(ct.getName()))
			{
				String type = ct.getType().getName();
//				body = body + "System.out.println(\""+ct.getName()+"\");\n";
//				System.out.println(ct.getName());	
				body = body + "for(int i=0; i < $1.length; i++)\n"
							+ "{\n"
							+ "if($1[i].equals(\""+ ct.getName() +"\")){\n"
									+ "check[i] = \"Here\"";

				if((ct.getType().getName()).equals("java.lang.String")){
					body = body + ct.getName()  + "= (String) $1[i+1];\n"
								+ "           		verifyVariable = true;\n"
								+ "           		i++;\n";
					}
				else{
					body = body + ct.getName() + "= ((" + convertTypes.get(type)  + ") $1[i+1])." + type + "Value();\n"
												+ " verifyVariable = true;\n"
												+ " i++;\n"; 
				}
				body = body  + "check[i] = \"Here\""
						+ " }"
						+ "} "
						+ "if(!verifyVariable)\n"
						+ "{\n"
						+ " " + ct.getName() + "=" + map.get(ct.getName()) + ";\n"
						+ "}\n"
						+ "else\n"
						+ "{\n"
						+ "     verifyVariable = false;\n"
						+ "}\n";
				map.remove(ct.getName());
				
			}
			else
			{
				//throw new UnrecognizeKeywordException(ct.getName());
			}
		}
		
		if(!map.isEmpty())
		{
			//throw new UnrecognizeKeywordException(); 
		}
		body = body + "for(int j = 0; j < check.length; j++)"
				+ "{"
				+ "if(!check[j].equals(\"Here\"))"
				+ "{"
				+ "		throw new UnrecognizeKeywordException($1[j]);"
				+ "}"
				+ "}"
				+ "}";
	//	System.out.println(body);
		
		ctConstructor.setBody(body);
	}
	
	public void SplitArgs(String args)
	{
		String[] paramValue; 
		String[] parts = args.split(",");
		for(String param : parts)
		{
			paramValue = param.split("=");
			if(!map.containsKey(paramValue[0])){
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