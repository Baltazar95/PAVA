package ist.meic.pa;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

public class MyTranslator implements Translator{
	
	public void start(ClassPool pool) throws NotFoundException, CannotCompileException {}
	
	public void onLoad(ClassPool pool, String className) throws NotFoundException, CannotCompileException {
		CtClass ctClass = pool.get(className); 	//pool - the ClassPool that this translator should use.
												//classname - the name of the class being loaded.
		makeUndoable(ctClass);
		//System.out.println("ola");
	}
	
	void makeUndoable(CtClass ctClass) throws NotFoundException, CannotCompileException {
		System.out.println("ola");
		final String template = "{" +
		        				"  History.storePrevious($0, \"%s\",\"%s\", ($w)$0.%s);" +
		        				"  $0.%s = $1;" +
		        				"}";
	    for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
	    	System.out.println(ctClass.getDeclaredMethods().getClass().getName());
	        ctMethod.instrument(new ExprEditor() {
	        	public void edit(FieldAccess fa) throws CannotCompileException {
	        		if (fa.isWriter()) {
	        			String name = fa.getFieldName();
	        			System.out.println(name);
	        			fa.replace(String.format(template, fa.getClassName(), name, name, name));
	        		}
	                }
	        	});
	    }
	}

}
