package main;

import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import main.ast.nodes.Program;
import main.compileError.CompileError;
import main.visitor.codeGenerator.CodeGenerator;
import main.visitor.type.TypeChecker;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import parsers.FunctionCraftLexer;
import parsers.FunctionCraftParser;

public class FunctionCraft {

	public static void main(String[] args) throws IOException {
		CharStream reader = CharStreams.fromFileName(args[0]);
		FunctionCraftLexer flLexer = new FunctionCraftLexer(reader);
		CommonTokenStream tokens = new CommonTokenStream(flLexer);
		FunctionCraftParser flParser = new FunctionCraftParser(tokens);
		Program program = flParser.program().flProgram;
		TypeChecker typeChecker = new TypeChecker();
		typeChecker.visit(program);
		typeChecker.typeErrors.sort(Comparator.comparingInt(CompileError::getLine));
		FileWriter fileWriter = new FileWriter("./samples/typeCheckErrors.txt");
		PrintWriter printWriter = new PrintWriter(fileWriter);
		for (CompileError compileError : typeChecker.typeErrors) {
			printWriter.println(compileError.getErrorMessage());
			System.out.println(compileError.getErrorMessage());
		}
		fileWriter.close();

		CodeGenerator codeGenerator = new CodeGenerator(typeChecker);
		codeGenerator.visit(program);

		codeGenerator.cleanMainJasminFile();

		// runJasminCode();
		runJasminCodeNotDeprecated();
	}

	private static void runJasminCodeNotDeprecated() {
		try {
			System.out.println("---------------------------Compilation Successful---------------------------");
			File dir = new File("./codeGenOutput");
			ProcessBuilder processBuilder = new ProcessBuilder("java", "-jar", "jasmin.jar", "*.j");
			processBuilder.directory(dir);
			Process process = processBuilder.start();
			// thread sleep to wait for the process to finish
			Thread.sleep(1000);
			processBuilder = new ProcessBuilder("java", "Main");
			processBuilder.directory(dir);
			process = processBuilder.start();
			printResults(process.getInputStream());
			printResults(process.getErrorStream());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void runJasminCode() {
		try {
			System.out.println("---------------------------Compilation Successful---------------------------");
			File dir = new File("./codeGenOutput");
			Process process = Runtime
				.getRuntime()
				.exec("java -jar jasmin.jar *.j", null, dir);
			Thread.sleep(1000);
			process = Runtime.getRuntime().exec("java Main", null, dir);
			printResults(process.getInputStream());
			printResults(process.getErrorStream());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void printResults(InputStream stream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line;
		try {
			while ((line = reader.readLine()) != null) System.out.println(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
