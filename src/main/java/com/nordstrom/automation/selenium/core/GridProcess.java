package com.nordstrom.automation.selenium.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openqa.grid.selenium.GridLauncher;
import org.testng.ITestResult;

class GridProcess {
	
	private static final String OPT_ROLE = "-role";
	
	static Process start(ITestResult testResult, String[] args) {
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		int optIndex = argsList.indexOf(OPT_ROLE);
		String gridRole = args[optIndex + 1];
		
		argsList.add(0, GridLauncher.class.getName());
		argsList.add(0, getClasspath(GridLauncher.class));
		argsList.add(0, "-cp");
		argsList.add(0, "c:\\tools\\java\\jdk1.8.0_112\\bin\\java");
		
		ProcessBuilder builder = new ProcessBuilder(argsList);
		
		String outputDir;
		if (testResult != null) {
			outputDir = testResult.getTestContext().getOutputDirectory();
		} else {
			Path currentRelativePath = Paths.get("");
			outputDir = currentRelativePath.toAbsolutePath().toString();
		}
		File outputFile = new File(outputDir, "grid-" + gridRole + ".log");
		
		builder.redirectErrorStream(true);
		builder.redirectOutput(outputFile);
		
		try {
			Files.createDirectories(outputFile.toPath().getParent());
			return builder.start();
		} catch (IOException e) {
			throw new RuntimeException("Failed to start grid " + gridRole + " process", e);
		}
	}
	
	private static String getClasspath(Class<?> clazz) {
		List<String> pathList = new ArrayList<>();
		URLClassLoader loader = (URLClassLoader) clazz.getClassLoader();
		for (URL url : loader.getURLs()) {
			pathList.add(new File(url.getPath()).getPath());
		}
		return String.join(File.pathSeparator, pathList);
	}
	
}
