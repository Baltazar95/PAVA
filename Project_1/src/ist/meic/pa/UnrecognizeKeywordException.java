package ist.meic.pa;

public class UnrecognizeKeywordException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private String keyword; 
	
	public UnrecognizeKeywordException(String newkeyword){
		keyword = newkeyword;
	}
	
	@Override
	public String toString()
	{
		return "Unrecognized Keyword: " + keyword; 
	}
}
