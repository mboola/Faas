package action;

import java.util.LinkedList;
import java.util.List;

public class MapFileAction implements Action<String, List<String>> {

	@Override
	public List<String> apply(String arg)
	{
		List<String>	strToReturn;
		Integer			numOfFiles = 10;
		int 			end, start;
		
		//experimental]
		strToReturn = new LinkedList<String>();

		int length = arg.length();
		int chunkSize = (int) Math.ceil((double) length / numOfFiles);
		start = 0;

		for (int i = 0; i < length; i += chunkSize)
		{
			end = Math.min(start + chunkSize, length);

			while (end < length && arg.charAt(end) != ' ') {
                end++;
            }

			strToReturn.add(arg.substring(start, end).trim());
            start = end;

			while (start < length && arg.charAt(start) == ' ') {
                start++;
            }
		}

		return (strToReturn);
	}

}
