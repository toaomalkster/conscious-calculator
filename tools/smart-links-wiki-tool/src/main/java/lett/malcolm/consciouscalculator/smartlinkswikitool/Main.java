/**
 * Conscious Calculator - Emulation of a conscious calculator.
 * Copyright © 2019 Malcolm Lett (malcolm.lett at gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package lett.malcolm.consciouscalculator.smartlinkswikitool;

import java.io.File;
import java.util.List;

/**
 * @author Malcolm Lett
 */
public class Main {

	public static void main(String[] args) throws Exception {
		String path;
		try {
			path = parseRequest(args);
		} catch (HelpRequestedException e) {
			PrintHelp();
			System.out.println();
			return;
		} catch (IllegalArgumentException e) {
			if (e.getMessage() != null) {
				System.out.println(e.getMessage());
				System.out.println();
			}
			PrintHelp();
			System.out.println();
			System.exit(1); // error
			return;
		}
		
		// check
		if (!new File(path).exists()) {
			throw new IllegalArgumentException("Path does not exist: " + path);
		}
		if (!new File(path).isDirectory()) {
			throw new IllegalArgumentException("Not a directory: " + path);
		}
		
		// run
		File root = new File(path).getCanonicalFile();
		System.out.println("Scanning " + root);
		
		DocumentMetadataScanner scanner = new DocumentMetadataScanner(root);
		List<DocumentInfo> docs = scanner.findDocuments();
		System.out.println(docs);
	}
	
	public static String parseRequest(String[] args) throws IllegalArgumentException, HelpRequestedException {
		if (args.length < 1) {
			throw new HelpRequestedException();
		}
		
		if (args.length < 1) {
			throw new IllegalArgumentException("Too many arguments");
		}
		
		for (int i=0; i < args.length; i++) {
			if (args[i].equals("--help")) {
				throw new HelpRequestedException();
			}
			else if (args[i].startsWith("/") || args[i].startsWith("-")) {
				throw new IllegalArgumentException("Unknown option: " + args[i]);
			}
		}
		
		return args[0];
	}
	
	public static void PrintHelp() {
		System.out.println("usage:");
		System.out.println("   java -jar smart-links-wiki-tool-xxx.jar path");
		System.out.println("where:");
		System.out.println("   path - root path to search for smart links");
	}
	
	public static class HelpRequestedException extends RuntimeException {
		
	}
	
}
