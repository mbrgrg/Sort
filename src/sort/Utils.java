package sort;

import java.util.Scanner;

public class Utils {
    
    public static void printLine(String message, Object ... args){
		System.out.println(replaceArguments(message, args));
    }
    
    public static void printLine(String message){
		System.out.println(message);
    }
    
    public static String readLine(String text){
    System.out.println(text);
    Scanner sc = new Scanner(System.in);
    String input = sc.nextLine();
    return input;
    }
    public static int readNumber(String text){
    System.out.println(text);
    Scanner sc = new Scanner(System.in);
    int input = sc.nextInt();
    return input;
    }
    
    private static final String replaceArguments(String message, Object ... args)
	{
		// Check if message is not null and there is at least one argument before doing replacement of arguments
		if (message != null && args != null && args.length > 0)
		{
			// Use a StringBuilder to increase performance
			StringBuilder sb = new StringBuilder();
			
			// Initialize next placeholder position to 0, so that the for cycle may be executed at least one time
			int lastPlaceHolderIndex = 0;
			
			// Iterate and replace placeholder with corresponding argument
			for (int i = 0; lastPlaceHolderIndex >= 0 && i < args.length; i++)
			{
				// Find the next placeholder position in the message
				int nextPlaceHolderIndex = message.indexOf("{}", lastPlaceHolderIndex);
				sb.append(message.substring(lastPlaceHolderIndex, nextPlaceHolderIndex)).append(args[i]);
				lastPlaceHolderIndex = nextPlaceHolderIndex + 2;
			}
			// If message wasn't processed completely, process the last part of message 
			if (lastPlaceHolderIndex < message.length())
			{
				sb.append(message.substring(lastPlaceHolderIndex));
			}
			return sb.toString();
		}
		return message;
	}
     
    interface Messages{
        public static final String ELAPSED_TIME = "Time to sort: {} seconds";
        public static final String ELAPSED_TIME_FILE = "Partial time: {} seconds";
        public static final String SPLIT_AND_SORT_FILE = "Sort number and write on file";
        public static final String SORT_END_FILE = "Sort ends, start to merge file";
        public static final String MERGE_END = "Merge file ends";
    }
   
}
