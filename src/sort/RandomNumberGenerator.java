package sort;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * <p>This program generates pseudo-random integer ten base numbers and writes them to a file.</p>
 * <p>The maximum value of generated numbers is equal to <code>2<sup>32</sup>-1</code>, the minimum value of generated numbers is <code>0</code>.<br> 
 * Generated numbers are written to the file as string in ten base representation.<br>
 * The file will contains a random number on each line and lines will be terminated with both carriage return (\r) and line feed (\n) characters.</p>
 */
public final class RandomNumberGenerator
{
	/* List of valid program arguments */
	private static final List<String> VALID_ARGUMENT_KEYS = Arrays.asList(ArgumentKeys.HELP_ARGUMENT, ArgumentKeys.FILE_SIZE_ARGUMENT, ArgumentKeys.FILE_PATH_ARGUMENT);

	/* The pseudo-random number generator */
	private static final Random RANDOM_GENERATOR = new Random();

	/**
	 * <p>The main method of the program.<br>
	 * It validates input arguments passed from command line and then call the generation method with the given parameters.</p>
	 * <p>Valid arguments are:
	 * 		<ul><li><b>-h</b> or <b>-H</b>: print out the help information, no file will be generated, any other option will be ignored, also invalid ones</li>
	 * 			<li><b>-o</b> or <b>-O &ltoutput_file_path&gt</b>: the path of the output file, if omitted <i>numbers.txt</i> will be used</li>
	 * 			<li><b>-s</b> or <b>-S &ltfile_size&gt</b>: the size of the file to be generated, if omitted <i>16 GiB</i> will be generated</li>
	 * 		</ul>
	 * </p>
	 * <p>Invalid arguments and errors will be cause program abort with specific status code:
	 * 		<ul><li>{@code 0}, program completed successfully</li>
	 * 			<li>{@code 1}, program aborted because of an unknown error</li>
	 * 			<li>{@code 2}, program aborted because of an I/O error, may be it does not have the write permission for the specified file or there is not enough space on the disk.</li>
	 * 			<li>{@code 10}, program aborted because of an unknown argument</li>
	 * 			<li>{@code 11}, program aborted because an invalid file size was specified</li>
	 * 			<li>{@code 12}, program aborted because an invalid file path was specified</li>
	 * 			<li>{@code 20}, program aborted because of user cancellation</li>
	 * 		</ul>
	 * </p>
	 *
	 * @param args {@link String} [] - Arguments passed to the program from command line
	 *
	 */
	public static void main(String ... args)
	{
		// Root try ... catch block intercept and print out unexpected errors
		try
		{
			// Get parsed program arguments
			Map<String, String> arguments = parseArguments(args);

			// Check if invalid arguments were found
			Set<String> invalidArguments = new HashSet<>(arguments.keySet()); // Create a copy of every argument found
			invalidArguments.removeAll(VALID_ARGUMENT_KEYS); // Remove from the set invalid arguments
			if (!invalidArguments.isEmpty()) // Check if the set contains any invalid argument
			{
				// Show error message, help information and exit with status code 10
				printError(Messages.ERROR_MESSAGE_INVALID_ARGUMENT, invalidArguments);
				showHelp();
				System.exit(StatusCodes.UNKNOWN_ARGUMENT);
			}

			// If help option exists, then print help information and exit
			if (arguments.containsKey(ArgumentKeys.HELP_ARGUMENT))
			{
				showHelp();
				System.exit(StatusCodes.SUCCESS); // Exit with status code 0
			}

			// Initialize the file size to the default value
			long targetFileSize = getLongArgumentValue(arguments, ArgumentKeys.FILE_SIZE_ARGUMENT, Constants.DEFAULT_TARGET_FILE_SIZE);

			// If the specified size is negative, print an error message and exit with error code 2
			if (targetFileSize < 0)
			{
				printError(Messages.ERROR_MESSAGE_INVALID_FILE_SIZE_VALUE, targetFileSize);
				System.exit(StatusCodes.INVALID_FILE_SIZE);
			}

			// Try to get the file path value from command line arguments; if it wasn't specified by user, the default file path will be used
			Path outputFile = Paths.get(getStringArgumentValue(arguments, ArgumentKeys.FILE_PATH_ARGUMENT, Constants.DEFAULT_OUTPUT_PATH));

			// Check if the specified file path already exists
			if (Files.exists(outputFile))
			{
				// If the file exists and is a regular file, ask to the user if he wants to overwrite existing file
				if (Files.isRegularFile(outputFile))
				{
					String choice = "y"; //showInputMessage(Messages.PROMPT_MESSAGE_OVERWRITE_EXISTING_FILE, outputFile.toAbsolutePath());
					/*if (choice == null || !choice.trim().equalsIgnoreCase("y"))
					{
						// User refused to overwrite the existing file, print warning and exit with status code 20
						printLine(Messages.ERROR_MESSAGE_USER_CANCELLED_PROGRAM_RUNNING);
						System.exit(StatusCodes.USER_CANCELLED);
					}*/
				}
				else // If the existing file is not a regular file (e.g. it is a folder, print an error message and exit with status code 12
				{
					printError(Messages.ERROR_MESSAGE_EXISTING_PATH_NOT_REGULAR_FILE, outputFile.toAbsolutePath());
					System.exit(StatusCodes.INVALID_FILE_PATH);
				}
			}

			printLine(Messages.INFO_MESSAGE_FILE_GENERATION_START, outputFile.toAbsolutePath().toString(), targetFileSize);

			// Get current time in milliseconds, it will be used to show the required time needed by file generation
			long startTime = System.currentTimeMillis();

			// Open an output stream for writing to the output file and call the generation method
			try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(outputFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING )))
			{
				generateFile(pw, targetFileSize, RANDOM_GENERATOR);
			}
			catch (IOException e)
			{
				printError(Messages.ERROR_MESSAGE_IO_ERROR);
				System.exit(StatusCodes.IO_ERROR);
			}

			printLine(Messages.INFO_MESSAGE_FILE_GENERATION_END, Files.size(outputFile));
			printLine(Messages.INFO_MESSAGE_FILE_GENERATION_ELAPSED_TIME, (System.currentTimeMillis() - startTime) / 1000);
		}
		catch (Throwable t)
		{
			printError(Messages.ERROR_MESSAGE_UNEXPECTED_ERROR, t, t.getMessage());
			System.exit(StatusCodes.ERROR);
		}
	}

	/**
	 * <p>Prints out help information about the usage of this program.</p>
	 *
	 */
	private static void showHelp()
	{
		printLine();
		printLine(Messages.INFO_MESSAGE_HELP);
		printLine();
	}

	/**
	 * <p>Generates and writes pseudo-random numbers to the specified output stream until the specified bytes are reached.<br>
	 * Only integer non-negative numbers will be generated and they will be written in ten based representation.</p>
	 *
	 * @param output {@link PrintWriter} - The output stream writer used to write pseudo-random numbers
	 * @param size {@code long} - The number of bytes to be written to the output stream
	 * @param generator {@link Random} - The pseudo-random number generator to be used
	 *
	 * @exception {@link IllegalArgumentException} - If any of the {@code output} or {@code generator} parameter is null or the {@code size} parameter is negative
	 *
	 */
	private static void generateFile(PrintWriter output, long size, Random generator)
	{
		/* Checks input parameters and launch an IllegalArgumentException if they are not valid */
		if (output == null)
		{
			throw new IllegalArgumentException("Output file writer cannot be null");
		}
		else if (size < 0)
		{
			throw new IllegalArgumentException("File size cannot be negative: " + size);
		}
		else if (generator == null)
		{
			throw new IllegalArgumentException("Random number generator cannot be null");
		}
		long startTime = System.currentTimeMillis();

		long writtenBytes = 0; // Number of bytes already written to the output
		int lastProgress = 0; // Last progress indicator on a scale from 0 to 100
		Long maxValue = null; // The maximum value generated
		Long minValue = null; // The minimum value generated

		// Generates random numbers until the target size is reached
		while (writtenBytes < size)
		{
			// If a number has already been written to the output, new line terminator
			if (writtenBytes > 0 && writtenBytes<size-1)
			{
				output.print("\r\n");
				writtenBytes = writtenBytes +2;
			}

			// Calculate the number of bytes that should be still written to the file
			long diff = size - writtenBytes;
			if(diff==0)
				break;
			//System.out.println("diff   "+diff);
			long number = 0;
			String value = null;
			if (diff > 10)
			{
				// Generate a new random non-negative integer number

				number = generator.nextInt() & 0xFFFFFFFFL;

				// Convert the generated number into ten based string representation
				value = String.valueOf(number);
			}
			else
			{
				// Iterate until the generated random number is long as diff value
				if(diff<0)
					diff*=-1;
				do
				{
					number = generator.nextInt((int)Math.pow(10, diff)) & 0xFFFFFFFFL;
					// Convert the generated number into ten based string representation
					value = String.valueOf(number);
				}
				while (value.length() < diff);
			}
			// If maxValue wasn't still set or is less then the generated number update its value
			if (maxValue == null || maxValue < number)
			{
				maxValue = number;
			}

			// If minValue wasn't still set or is greater then the generated number update its value
			if (minValue == null || minValue > number)
			{
				minValue = number;
			}

			// Write the value to the output
			output.print(value);

			// Increment the number of bytes written to the output
			if(writtenBytes<size)
				writtenBytes = writtenBytes + value.length();

			// Calculate the generation progress
			int currentProgress = (int)((writtenBytes * 100) / size);

			// If the current progress is greater then the previous one and do not exceed 100, update and print it
			//System.out.println("writtenbytes   "+writtenBytes);
			if (currentProgress > lastProgress && currentProgress <= 100)
			{
				lastProgress = currentProgress;
				long eta = (System.currentTimeMillis() - startTime) / (10 * currentProgress);
				printLine(Messages.INFO_MESSAGE_FILE_GENERATION_PROGRESS, currentProgress, writtenBytes, size, eta);
			}
		}

		// Force flushing of content to the output
		output.flush();

		// If at least one number was generated, print the maximum value of generated numbers
		if (maxValue != null)
		{
			printLine(Messages.INFO_MESSAGE_MAXIMUM_GENERATED_VALUE, maxValue);
		}

		// If at least one number was generated, print the minimum value of generated numbers
		if (minValue != null)
		{
			printLine(Messages.INFO_MESSAGE_MINIMUM_GENERATED_VALUE, minValue);
		}
	}

	/**
	 * <p>Parses input arguments passed to the program and converts them into a dictionary (map).<br>
	 * The arguments names (keys) are case insensitive.</p>
	 *
	 * @param args {@link String} [] - Command line arguments to be parsed
	 *
	 * @return {@link Map}&ltString, String&gt - Returns the parsed program arguments as a map (key, value). If {@code args} parameter is empty or {@code null}, an empty map will be returned
	 *
	 */
	private static final Map<String, String> parseArguments(String ... args)
	{
		// Initialize the map that will contains parsed arguments
		Map<String, String> arguments = new HashMap<>();

		// Iterate given arguments list, if it is null, no iteration is done
		for (int i = 0; args != null && i < args.length; i++)
		{
			// The first parameter in the arguments list is the argument name (key)
			String key = args[i];

			// If the key is equals to the help argument, then no value is associated to it and a null value will be put into the map
			if (ArgumentKeys.HELP_ARGUMENT.equalsIgnoreCase(key))
			{
				arguments.put(key.toLowerCase(), null);
			}
			// Else if the key is not null and there is a value associated to the option, insert it into the map
			else if (key != null)
			{
				if (++i < args.length)
				{
					arguments.put(key, args[i]);
				}
				else
				{
					arguments.put(key, null);
				}
			}
		}
		return arguments;
	}

	/**
	 * <p>Returns the value of the given argument in the specified map.</p>
	 *
	 * @param arguments {@link Map}&ltString,String&gt - The arguments map where to search for given argument value
	 * @param argumentName {@link String} - The name of argument to search for
	 * @param defaultValue {@link String} - The value to be returned if the given argument does not exist
	 *
	 * @return {@link String} - The value of the given argument or the default value if it was not found
	 *
	 */
	private static final String getStringArgumentValue(Map<String, String> arguments, String argumentName, String defaultValue)
	{
		if (arguments != null && arguments.containsKey(argumentName))
		{
			return arguments.get(argumentName);
		}
		return defaultValue;
	}

	/**
	 * <p>Returns the value of the output file size.<br>
	 * It tries to get the value from program command arguments, if that option was not specified, the default value will be returned.</p>
	 *
	 * @param arguments {@link Map}&ltString,String&gt - The arguments map where to search for the output file size
	 * @param argumentName {@link String} - The name of argument to search for
	 * @param defaultValue {@link String} - The value to be returned if the output file size argument does not exist
	 *
	 * @return {@code long} - The size of the output file
	 *
	 */
	private static final long getLongArgumentValue(Map<String, String> arguments, String argumentName, long defaultValue)
	{
		// Initialize the file size to the default value
		long targetFileSize = defaultValue;

		try
		{
			String argumentValue = getStringArgumentValue(arguments, argumentName, null);

			// If file size option exists, try to parse its value
			if (argumentValue != null)
			{
				targetFileSize = Long.parseLong(argumentValue);
			}
		}
		catch (Exception e)
		{
			// An invalid size value was provided, print an error message and exit with status code 11
			printError(Messages.ERROR_MESSAGE_WRONG_FILE_SIZE_FORMAT, arguments.get(ArgumentKeys.FILE_SIZE_ARGUMENT));
			System.exit(StatusCodes.INVALID_FILE_SIZE);
		}
		return targetFileSize;
	}

	/**
	 * <p>Prints an empty line to the output terminal.</p>
	 *
	 */
	private static final void printLine()
	{
		System.out.println();
	}

	/**
	 * <p>Prints a line to the output terminal.</p>
	 *
	 * @param message {@link String} - The message to print, if it contains placeholder <code>{}</code>, they will be replaced with the specified arguments
	 * @param args {@link Object} [] - The arguments to be used for replacing placeholder into the message
	 *
	 */
	private static final void printLine(String message, Object ... args)
	{
		System.out.println(replaceArguments(message, args));
	}

	/**
	 * <p>Prints an message on the error terminal.</p>
	 *
	 * @param message {@link String} - The message to print, if it contains placeholder <code>{}</code>, they will be replaced with the specified arguments
	 * @param args {@link Object} [] - The arguments to be used for replacing placeholder into the message
	 *
	 */
	private static final void printError(String message, Object ... args)
	{
		System.err.println(replaceArguments(message, args));
	}

	/**
	 * <p>Prints an error on the error terminal.</p>
	 *
	 * @param message {@link String} - The message to print, if it contains placeholder <code>{}</code>, they will be replaced with the specified arguments
	 * @param error {@link Throwable} - The exception to be printed out
	 * @param args {@link Object} [] - The arguments to be used for replacing placeholder into the message
	 *
	 */
	private static final void printError(String message, Throwable error, Object ... args)
	{
		System.err.println(replaceArguments(message, args));
		error.printStackTrace();
	}


	/**
	 * <p>Replaces placeholder in the message string with the given arguments.</p>
	 *
	 * @param message {@link String} - The message to processed, if it contains placeholder <code>{}</code>, they will be replaced with the specified arguments
	 * @param args {@link Object} [] - The arguments to be used for replacing placeholder into the message
	 *
	 */
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

	/**
	 * <p>Ask an input to the user.</p>
	 *
	 * @param message {@link String} - The message to show to the user, if it contains placeholder <code>{}</code>, they will be replaced with the specified arguments
	 * @param args {@link Object} [] - The arguments to be used for replacing placeholder into the message
	 *
	 * @return {@link String} - Returns the input of the user
	 *
	 */
	private static final String showInputMessage(String message, Object ... args)
	{
		System.out.print(replaceArguments(message, args));

		// Read user input
		try (Scanner scanner = new Scanner(System.in))
		{
			return scanner.nextLine();
		}
	}
}

/* List of information messages shown to the user */
interface InfoMessages
{
	public static final String INFO_MESSAGE_FILE_GENERATION_START = "Generating file '{}' {} bytes long...";
	public static final String INFO_MESSAGE_FILE_GENERATION_END = "Generated file size: {} bytes";
	public static final String INFO_MESSAGE_FILE_GENERATION_ELAPSED_TIME = "Elapsed time: {} seconds";
	public static final String INFO_MESSAGE_FILE_GENERATION_PROGRESS = "Progress: {}% ({} / {}), ETA: {} s";
	public static final String INFO_MESSAGE_MAXIMUM_GENERATED_VALUE = "Maximum generated value: {}";
	public static final String INFO_MESSAGE_MINIMUM_GENERATED_VALUE = "Minimum generated value: {}";
	public static final String INFO_MESSAGE_HELP = "Generates a text file with integer ten based pseudo-random numbers, one for each line of the file.\r\n\r\n" +
			"Usage:\r\n" +
			"\tjava RandomNumberGenerator [-h/H] [-o/O <output_file_path>] [-s/S <target_file_size>]\r\n" +
			"\t\t-o or -O: use to specify the output file path, if omitted the default value '" + Constants.DEFAULT_OUTPUT_PATH + "' will be used\r\n" +
			"\t\t-s or -S: use to specify the target file size, if omitted the default value '" + Constants.DEFAULT_TARGET_FILE_SIZE + "' bytes will be used\r\n" +
			"\t\t-h or -H: use to print this help page, any other option will be ignored\r\n\r\n" +
			"Examples:\r\n" +
			"\t java RandomNumberGenerator\r\n" +
			"\t\t\tCreates a file with default name and size in the current folder\r\n\r\n" +
			"\t java RandomNumberGenerator -h\r\n" +
			"\t\t\tPrints help page\r\n\r\n" +
			"\t java RandomNumberGenerator -s 1000\r\n" +
			"\t\t\tCreates a 1 KB file named 'numbers.txt' in the current folder\r\n\r\n" +
			"\t java RandomNumberGenerator -s 1000 -o \"../random.txt\"\r\n" +
			"\t\t\tCreates a 1 KB file named 'random.txt' in the parent folder";
}

/* List of error messages shown to the user */
interface ErrorMessages
{
	public static final String ERROR_MESSAGE_WRONG_FILE_SIZE_FORMAT = "Invalid file size: '{}', it must be a valid long number.";
	public static final String ERROR_MESSAGE_INVALID_FILE_SIZE_VALUE = "Invalid file size: '{}', it must be a non-negative long number.";
	public static final String ERROR_MESSAGE_USER_CANCELLED_PROGRAM_RUNNING = "User cancelled program execution.";
	public static final String ERROR_MESSAGE_EXISTING_PATH_NOT_REGULAR_FILE = "The specified output file already exists, but it isn't a regular file: {}";
	public static final String ERROR_MESSAGE_UNEXPECTED_ERROR = "An unexpected error occurred: {}";
	public static final String ERROR_MESSAGE_INVALID_ARGUMENT = "The following arguments are not valid: {}";
	public static final String ERROR_MESSAGE_IO_ERROR = "An I/O error occurred: be sure the program have write permissions to the specified file and that there is enough space to the disk.";
}

/* List of prompt messages shown to the user */
interface PromptMessages
{
	public static final String PROMPT_MESSAGE_OVERWRITE_EXISTING_FILE = "The specified output file '{}' already exists, do you want to overwrite it? (y/N): ";
}

/* List of every above message */
interface Messages extends ErrorMessages, InfoMessages, PromptMessages
{
}

/* Names (keys) of arguments accepted by this program */
interface ArgumentKeys
{
	public static final String HELP_ARGUMENT = "-h";
	public static final String FILE_PATH_ARGUMENT = "-o";
	public static final String FILE_SIZE_ARGUMENT = "-s";
}

/* List of status code returned by this program */
interface StatusCodes
{
	public static final int SUCCESS = 0;
	public static final int ERROR = 1;
	public static final int IO_ERROR = 2;
	public static final int UNKNOWN_ARGUMENT = 10;
	public static final int INVALID_FILE_SIZE = 11;
	public static final int INVALID_FILE_PATH = 12;
	public static final int USER_CANCELLED = 20;
}

/* Constants and default values used by this program */
interface Constants
{
	/* The number of bytes in 1 GiB */
	public static final int ONE_GiB = 1 << 25;

	/* The target size of the file to be generated */
	public static final long DEFAULT_TARGET_FILE_SIZE = 16L * ONE_GiB;

	/* The default output file path */
	public static final String DEFAULT_OUTPUT_PATH = "file.txt";
}
