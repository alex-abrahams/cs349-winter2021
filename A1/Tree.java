import java.util.ArrayList;
import java.io.File;
import java.nio.file.*; 


public class Tree {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	static int maxDepth = 1;
	static boolean colour = true;
	static boolean directoriesOnly = false;
	static boolean showHidden = false;
	static String initialDir = ".";
	
	private static void showHelp() {
		System.out.println("$ tree -help");
		System.out.println("Usage: tree [dir] [-option [parameter]]");
		System.out.println("[dir]                     :: directory to start traversal [.]");
		System.out.println("-help                     :: display this help and exit [false]");
		System.out.println("-c true|false             :: show entries colorized [true]");
		System.out.println("-d                        :: list directories only [false]");
		System.out.println("-l n                      :: maximum display depth [1]");
		System.out.println("-a                        :: show hidden files [false]");
	}
	private static void doTabs(int depth) {
		for (int i=0;i<depth;i++) {
			if (i == depth-1) {
				System.out.print("└── ");
			} else {
				System.out.print("    ");
			}
		}
	}
	private static String colourIfColour(File f) {
		if (!colour) {
			return ANSI_RESET;
		} else if (f.isDirectory()) {
			return ANSI_GREEN;
		} else if (Files.isExecutable(f.toPath())) {
			return ANSI_RED;
		} else {
			return ANSI_RESET;
		}
	}
	private static void doDirectory(File dir, int depth) {
		File[] files = dir.listFiles();
		for (File f: files) {
			if (!f.isHidden() || showHidden) {
				doTabs(depth);
				if (f.isDirectory()) {
					System.out.println(colourIfColour(f) + f.getName() + ANSI_RESET);
					if (depth < maxDepth-1) {
						doDirectory(f, depth + 1);
					}
				} else if (!directoriesOnly) {
					System.out.println(colourIfColour(f) + f.getName() + ANSI_RESET);
				}
			}
		}
	}
	public static void main(String[] args) {
		int length = args.length;
		for (int i=0;i<length;i++) {
			if (i == 0 && !(args[i].charAt(0) == '-')) {
				// directory
				initialDir = args[i];
			}
			if (args[i].toLowerCase().equals("-c")) {
				i++;
				if (args[i].toLowerCase().equals("false")) {
					colour = false;
				} else {
					colour = true;
				}
			}
			if (args[i].toLowerCase().equals("-help")) {
				showHelp();
				return;
			}
			if (args[i].toLowerCase().equals("-d")) {
				directoriesOnly = true;
			}
			if (args[i].toLowerCase().equals("-a")) {
				showHidden = true;
			}
			if (args[i].toLowerCase().equals("-l")) {
				i++;
				maxDepth = Integer.parseInt(args[i]);
				if (maxDepth < 1) {
					System.out.println("Depth must be greater than 0");
					maxDepth = 1;
				}
			}
		}
		File dir = new File(initialDir);
		doDirectory(dir, 0);
	}
}
