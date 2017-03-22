package ist.meic.pa;

import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import javassist.Translator;


public class MyTranslator implements Translator
{
	public Map<String, String> map = new HashMap<String, String>();
	
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {}
	
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
	
	public void CheckKeywordArgs(CtClass ctClass) throws ClassNotFoundException 
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
	
	public void Injection(CtClass ctClass, CtConstructor ctConstructor, Object annotation)
	{
		//FIXME continue this
		//ctClass.getDeclaredFields();
	}
	
	public void SplitArgs(String args)
	{
		String[] paramValue; 
		String[] parts = args.split(",");
		for(String param : parts)
		{
			paramValue = param.split("=");
			map.put(paramValue[0], paramValue[1]);
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
