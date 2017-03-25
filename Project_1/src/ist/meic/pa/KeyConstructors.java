package ist.meic.pa;

import javassist.ClassPool;
import javassist.Loader;
import javassist.Translator;

public class KeyConstructors 
{
	public static void main(String[] args) throws Throwable
	{
		if(args.length == 0)
		{
			System.err.println("Profiler needs arguments");	
			return;
		}	
		Translator translator = new MyTranslator();
		ClassPool pool = ClassPool.getDefault();
		Loader classLoader = new Loader();
		classLoader.addTranslator(pool, translator);
		String[] restArgs = new String[args.length - 1];
		System.arraycopy(args, 1, restArgs, 0, restArgs.length);
		classLoader.run(args[0], restArgs);		
	}
}
