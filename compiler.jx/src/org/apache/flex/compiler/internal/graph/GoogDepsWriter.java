package org.apache.flex.compiler.internal.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;

import com.google.common.io.Files;

public class GoogDepsWriter {

	public GoogDepsWriter(File outputFolder, String mainClassName, JSGoogConfiguration config)
	{
		this.outputFolderPath = outputFolder.getAbsolutePath();
		this.mainName = mainClassName;
		googPath = config.getClosureLib();
		otherPaths = config.getSDKJSLib();
	}
	
	private String outputFolderPath;
	private String mainName;
	private String googPath;
	private List<String> otherPaths;
	
	private HashMap<String,GoogDep> depMap = new HashMap<String,GoogDep>();
	
	public ArrayList<String> getListOfFiles() throws InterruptedException
	{
		buildDB();
		ArrayList<GoogDep> dps = sort(mainName);
		ArrayList<String> files = new ArrayList<String>();
		for (GoogDep gd : dps)
		{
			files.add(gd.filePath);
		}
		return files;
	}
	
	public String generateDeps() throws InterruptedException, FileNotFoundException
	{
		buildDB();
		ArrayList<GoogDep> dps = sort(mainName);
		String outString = "// generated by FalconJS" + "\n";
		int n = dps.size();
		for (int i = n - 1; i >= 0; i--)
		{
			GoogDep gd = dps.get(i);
			String s = "goog.addDependency('";
			s += relativePath(gd.filePath);
			s += "', ['";
			s += gd.className;
			s += "'], [";
			s += getDependencies(gd.deps);
			s += "]);\n";
			outString += s;
		}
		return outString;
	}
	
	private void buildDB()
	{
		addDeps(mainName);
	}
	
	private HashMap<String, GoogDep> visited = new HashMap<String, GoogDep>();
	
	private ArrayList<GoogDep> sort(String rootClassName)
	{
		ArrayList<GoogDep> arr = new ArrayList<GoogDep>();
		GoogDep current = depMap.get(rootClassName);
		sortFunction(current, arr);
		return arr;
	}
	
	private void sortFunction(GoogDep current, ArrayList<GoogDep> arr)
	{
		visited.put(current.className, current);

		ArrayList<String> deps = current.deps;
		for (String className : deps)
		{
			if (!visited.containsKey(className))
			{
				GoogDep gd = depMap.get(className);
				sortFunction(gd, arr);
			}
		}
		arr.add(current);
	}
	
	private void addDeps(String className)
	{
		if (depMap.containsKey(className))
			return;
		
		// build goog dependency list
		GoogDep gd = new GoogDep();
		gd.className = className;
		gd.filePath = getFilePath(className);
		depMap.put(gd.className, gd);
		ArrayList<String> deps = getDirectDependencies(gd.filePath);
		gd.deps = new ArrayList<String>();
		ArrayList<String> circulars = new ArrayList<String>();
		for (String dep : deps)
		{
		    if (depMap.containsKey(dep))
		    {
		        circulars.add(dep);
		        continue;
		    }
			gd.deps.add(dep);
			addDeps(dep);
		}
		if (circulars.size() > 0)
		{
		    // remove requires that would cause circularity
		    try
            {
                List<String> fileLines = Files.readLines(new File(gd.filePath), Charset.defaultCharset());
                ArrayList<String> finalLines = new ArrayList<String>();
                
                String inherits = getBaseClass(fileLines, className);
                
                for (String line : fileLines)
                {
                    int c = line.indexOf("goog.require");
                    if (c > -1)
                    {
                        int c2 = line.indexOf(")");
                        String s = line.substring(c + 14, c2 - 1);
                        if (circulars.contains(s) && !s.equals(inherits))
                            continue;
                    }
                    finalLines.add(line);
                }
                File file = new File(gd.filePath);  
                PrintWriter out = new PrintWriter(new FileWriter(file));  
                for (String s : finalLines)
                {
                    out.println(s);
                }
                out.close();
                    
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
		    
		}
	}
	
	String getBaseClass(List<String> lines, String className)
	{
	    int n = lines.size();
	    for (int i = 0; i < n; i++)
	    {
	        String line = lines.get(i);
	        int c2;
	        int c = line.indexOf("goog.inherits");
	        if (c > -1)
	        {
	            String inheritLine = ""; 
                while (true)
                {
                    inheritLine += line;
                    c2 = line.indexOf(")");
                    if (c2 > -1)
                        break;
                    else
                    {
                        i++;
                        line = lines.get(i);
                    }
                }
	            c = inheritLine.indexOf(",");
                c2 = inheritLine.indexOf(")");
                return inheritLine.substring(c + 1, c2).trim();            
	        }
	    }
	    System.out.println("couldn't find base class for " + className);
	    return null;
	}
	
	String getFilePath(String className)
	{
	    String fn;
	    File destFile;
	    File f;
	    
		System.out.println("Finding file for class: " + className);
		String classPath = className.replace(".", File.separator);
		if (className.contains("goog."))
		{
		    String googPackage = classPath.substring(0, classPath.lastIndexOf(File.separator));
		    String googClass = classPath.substring(classPath.lastIndexOf(File.separator) + 1);
    		fn = googPath + File.separator + "closure" + File.separator + googPackage + File.separator + googClass.toLowerCase() + ".js";
    		f = new File(fn);
    		if (f.exists())
    		{
    		    fn = outputFolderPath + File.separator + "library" +
    		        File.separator + "closure" + 
    		        File.separator + googPackage + File.separator + googClass.toLowerCase() + ".js";
    		    destFile = new File(fn);
                // copy source to output
                try {
                    FileUtils.copyFile(f, destFile);
                    System.out.println("Copying file for class: " + className);
                } catch (IOException e) {
                    System.out.println("Error copying file for class: " + className);
                }
    			return fn;
    		}
    		
            fn = googPath + File.separator + "third_party" + File.separator + "closure" + File.separator + googPackage + File.separator + googClass.toLowerCase() + ".js";
            f = new File(fn);
            if (f.exists())
            {
                fn = outputFolderPath + File.separator + "library" +
                    File.separator  + "third_party" + File.separator + "closure" + 
                    File.separator + googPackage + File.separator + googClass.toLowerCase() + ".js";
                destFile = new File(fn);
                // copy source to output
                try {
                    FileUtils.copyFile(f, destFile);
                    System.out.println("Copying file for class: " + className);
                } catch (IOException e) {
                    System.out.println("Error copying file for class: " + className);
                }
                return fn;
            }
            // Closure Library also uses just goog.provide(goog.foo) in goog/foo/foo.js
            fn = googPath + File.separator + "closure" + File.separator + 
                                googPackage + File.separator + googClass.toLowerCase() + File.separator + googClass.toLowerCase() + ".js";
            f = new File(fn);
            if (f.exists())
            {
                fn = outputFolderPath + File.separator + "library" +
                    File.separator + "closure" + 
                    File.separator + googPackage + File.separator + googClass.toLowerCase() + File.separator + googClass.toLowerCase() + ".js";
                destFile = new File(fn);
                // copy source to output
                try {
                    FileUtils.copyFile(f, destFile);
                    System.out.println("Copying file for class: " + className);
                } catch (IOException e) {
                    System.out.println("Error copying file for class: " + className);
                }
                return fn;
            }
            
            fn = googPath + File.separator + "third_party" + File.separator + "closure" + File.separator +
                            googPackage + File.separator + googClass.toLowerCase() + File.separator + googClass.toLowerCase() + ".js";
            f = new File(fn);
            if (f.exists())
            {
                fn = outputFolderPath + File.separator + "library" +
                    File.separator  + "third_party" + File.separator + "closure" +
                    File.separator + googPackage + File.separator + googClass.toLowerCase() + File.separator + googClass.toLowerCase() + ".js";
                destFile = new File(fn);
                // copy source to output
                try {
                    FileUtils.copyFile(f, destFile);
                    System.out.println("Copying file for class: " + className);
                } catch (IOException e) {
                    System.out.println("Error copying file for class: " + className);
                }
                return fn;
            }
            // if we still haven't found it, use this hack for now.  I guess
            // eventually we should search every file.
            if (googClass.equals("ListenableKey"))
                googClass = "Listenable";
            else if (googClass.equals("EventLike"))
                googClass = "Event";
            fn = googPath + File.separator + "closure" + File.separator + googPackage + File.separator + googClass.toLowerCase() + ".js";
            f = new File(fn);
            if (f.exists())
            {
                fn = outputFolderPath + File.separator + "library" +
                    File.separator + "closure" + 
                    File.separator + googPackage + File.separator + googClass.toLowerCase() + ".js";
                destFile = new File(fn);
                // copy source to output
                try {
                    FileUtils.copyFile(f, destFile);
                    System.out.println("Copying file for class: " + className);
                } catch (IOException e) {
                    System.out.println("Error copying file for class: " + className);
                }
                return fn;
            }
		}
		
        fn = outputFolderPath + File.separator + classPath + ".js";
        f = new File(fn);
        if (f.exists())
        {
            return fn;
        }
        
        for (String otherPath : otherPaths)
        {
    		fn = otherPath + File.separator + classPath + ".js";
    		f = new File(fn);
    		if (f.exists())
    		{
    			fn = outputFolderPath + File.separator + classPath + ".js";
    			destFile = new File(fn);
    			// copy source to output
    			try {
    				FileUtils.copyFile(f, destFile);
    				System.out.println("Copying file for class: " + className);
    			} catch (IOException e) {
    				System.out.println("Error copying file for class: " + className);
    			}
    			return fn;
    		}
        }
        
		System.out.println("Could not find file for class: " + className);
		return "";
	}
	
	private ArrayList<String> getDirectDependencies(String fn)
	{
		ArrayList<String> deps = new ArrayList<String>();
		
		FileInputStream fis;
		try {
			fis = new FileInputStream(fn);
			Scanner scanner = new Scanner(fis, "UTF-8");
			while (scanner.hasNextLine())
			{
				String s = scanner.nextLine();
				if (s.indexOf("goog.inherits") > -1)
					break;
				int c = s.indexOf("goog.require");
				if (c > -1)
				{
					int c2 = s.indexOf(")");
					s = s.substring(c + 14, c2 - 1);
					deps.add(s);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return deps;
	}
	
	private String getDependencies(ArrayList<String> deps)
	{
		String s = "";
		for (String dep : deps)
		{
			if (s.length() > 0)
			{
				s += ", ";
			}
			s += "'" + dep + "'";			
		}
		return s;
	}

	String relativePath(String path)
	{
        if (path.indexOf(outputFolderPath) == 0)
        {
            path = path.replace(outputFolderPath, "../../..");
        }
        else
        {
    	    for (String otherPath : otherPaths)
    	    {
        		if (path.indexOf(otherPath) == 0)
        		{
        			path = path.replace(otherPath, "../../..");
        			
        		}
    	    }
        }
		// paths are actually URIs and always have forward slashes
		path = path.replace('\\', '/');
		return path;
	}
	private class GoogDep
	{
		public String filePath;
		public String className;
		public ArrayList<String> deps;
		
	}
}
