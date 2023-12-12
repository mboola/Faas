package action;

public class MapFileAction implements Action<String, String[]> {

	@Override
	public String[] apply(String arg)
	{
		String[]	strToReturn;
		Integer		len;
		Integer		numOfFiles = 10;
		
		strToReturn = new String[numOfFiles];
		len = arg.length() / numOfFiles;
		
		//find next space from len.
		//once I find it, substr til len * 2, also space.
		//add to strToReturn
		//this numOfFiles times

		return (strToReturn);
	}

}
