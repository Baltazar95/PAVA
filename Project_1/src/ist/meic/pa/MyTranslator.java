package ist.meic.pa;

import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.Translator;


public class MyTranslator implements Translator
{
	public Map<String, String> map = new HashMap<String, String>();
	
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {}
	
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException 
	{
		CtClass ctClass = pool.get(className); 	//pool - the ClassPool that this translator should use.
		System.out.println(ctClass.getName());//classname - the name of the class being loaded.
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
		for(CtMethod ctMethod: ctClass.getDeclaredMethods())
		{
			//This loop will enable the programmers to have other annotations because it only cares with the KeywordArgs annotation
			for(Object annotation: ctMethod.getAnnotations())
			{
				if(annotation instanceof KeywordArgs)
				{
					SplitArgs(((KeywordArgs) annotation).value());
					Injection(ctClass, ctMethod, annotation);
					break;
				}
			}
		}
	}
	
	public void Injection(CtClass ctClass, CtMethod ctMethod, Object annotation)
	{
		
	}
	
	public void SplitArgs(String args)
	{
		System.out.println(args);
	}
}
