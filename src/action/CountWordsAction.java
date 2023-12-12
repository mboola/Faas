package action;

public class CountWordsAction implements Action<String, Long>  {

	@Override
	public Long apply(String arg)
	{
		String[]			words;

		words = arg.split(" ");
		return (long) (words.length);
	}
    
}
