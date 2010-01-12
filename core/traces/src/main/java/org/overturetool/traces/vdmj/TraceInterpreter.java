package org.overturetool.traces.vdmj;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.ListIterator;

import org.overturetool.traces.utility.TraceXmlWrapper;
import org.overturetool.vdmj.Release;
import org.overturetool.vdmj.Settings;
import org.overturetool.vdmj.commands.ModuleCommandReader;
import org.overturetool.vdmj.definitions.ClassDefinition;
import org.overturetool.vdmj.definitions.ClassList;
import org.overturetool.vdmj.definitions.Definition;
import org.overturetool.vdmj.definitions.NamedTraceDefinition;
import org.overturetool.vdmj.lex.Dialect;
import org.overturetool.vdmj.lex.LexLocation;
import org.overturetool.vdmj.lex.LexTokenReader;
import org.overturetool.vdmj.modules.Module;
import org.overturetool.vdmj.modules.ModuleList;
import org.overturetool.vdmj.runtime.ClassInterpreter;
import org.overturetool.vdmj.runtime.Context;
import org.overturetool.vdmj.runtime.ContextException;
import org.overturetool.vdmj.runtime.Interpreter;
import org.overturetool.vdmj.runtime.ModuleInterpreter;
import org.overturetool.vdmj.runtime.ObjectContext;
import org.overturetool.vdmj.runtime.StateContext;
import org.overturetool.vdmj.runtime.ValueException;
import org.overturetool.vdmj.syntax.ClassReader;
import org.overturetool.vdmj.syntax.ModuleReader;
import org.overturetool.vdmj.traces.CallSequence;
import org.overturetool.vdmj.traces.TestSequence;
import org.overturetool.vdmj.traces.Verdict;
import org.overturetool.vdmj.typechecker.ClassTypeChecker;
import org.overturetool.vdmj.typechecker.Environment;
import org.overturetool.vdmj.typechecker.FlatEnvironment;
import org.overturetool.vdmj.typechecker.ModuleEnvironment;
import org.overturetool.vdmj.typechecker.ModuleTypeChecker;
import org.overturetool.vdmj.typechecker.PrivateClassEnvironment;
import org.overturetool.vdmj.typechecker.PublicClassEnvironment;
import org.overturetool.vdmj.typechecker.TypeChecker;
import org.overturetool.vdmj.values.ObjectValue;

public class TraceInterpreter
{
	protected long beginClass = 0;
	protected long beginTrace = 0;
	protected String activeClass = "";
	protected String activeTrace;

	Interpreter interpreter;

	public void processTraces(List<File> specFiles, String className,
			TraceXmlWrapper storage, boolean runTypeCheck, Dialect dialect,
			Release languageVersion) throws Exception
	{

		Settings.prechecks = true;
		Settings.postchecks = true;
		Settings.dynamictypechecks = true;
		Settings.dialect = dialect;
		Settings.release = languageVersion;

		if (dialect == Dialect.VDM_PP || dialect == Dialect.VDM_RT)
			processTracesClasses(specFiles,
					className,
					storage,
					runTypeCheck,
					dialect);
		else if (dialect == Dialect.VDM_SL)
			processTracesModules(specFiles,
					className,
					storage,
					runTypeCheck,
					dialect);
	}

	private void processTracesClasses(List<File> specFiles, String className,
			TraceXmlWrapper storage, boolean runTypeCheck, Dialect dialect)
			throws Exception
	{
		ClassList classes = new ClassList();
		int parsErrors = 0;
		for (File file : specFiles)
		{
			LexTokenReader ltr;

			ltr = new LexTokenReader(file, dialect);

			ClassReader mr = new ClassReader(ltr);
			parsErrors += mr.getErrorCount();
			if (mr.getErrorCount() > 0)
			{
				Writer parseErrors = new StringWriter();
				mr.printErrors(new PrintWriter(parseErrors));
				parseError(parseErrors.toString());
			}
			classes.addAll(mr.readClasses());
		}

		if (parsErrors == 0)
		{
			

			

			processTrace(classes,className,runTypeCheck,storage);
		}
	}
	public void processTrace(ClassList classList,String className, boolean runTypeCheck, TraceXmlWrapper storage) throws Exception
	{
		if(runTypeCheck)
		{
			TypeChecker tc = new ClassTypeChecker(classList);
			if (runTypeCheck)
			{
				typeCheckStarted();
				tc.typeCheck();
				classList.setLoaded(); // do not type check this classes again
			}
			if (TypeChecker.getErrorCount() == 0)
			{
				interpreter = new ClassInterpreter(classList);

			} else
			{
				Writer typeErrors = new StringWriter();
				TypeChecker.printErrors(new PrintWriter(typeErrors));
				typeError(typeErrors.toString());
			}
		}else
			interpreter = new ClassInterpreter(classList);
		
		interpreter.init(null);
		ClassDefinition classdef 	= interpreter.findClass(className);
		
		if (classdef == null)
		{
			error("Class not found: " + className);
			throw new ClassNotFoundException(className);
		}
		
		processTraces(classdef,classdef.definitions ,className, storage);
	}
	
	
	private void processTracesModules(List<File> specFiles, String moduleName,
			TraceXmlWrapper storage, boolean runTypeCheck, Dialect dialect)
			throws Exception
	{
		ModuleList modules = new ModuleList();
		int parsErrors = 0;
		for (File file : specFiles)
		{
			LexTokenReader ltr;

			ltr = new LexTokenReader(file, dialect);

			ModuleReader mr = new ModuleReader(ltr);
			parsErrors += mr.getErrorCount();
			if (mr.getErrorCount() > 0)
			{
				Writer parseErrors = new StringWriter();
				mr.printErrors(new PrintWriter(parseErrors));
				parseError(parseErrors.toString());
			}
			modules.addAll(mr.readModules());
		}

		if (parsErrors == 0)
		{
		
			processTrace(modules,moduleName,runTypeCheck,storage);
		}
	}
	
	
	public void processTrace(ModuleList moduleList,String moduleName, boolean runTypeCheck, TraceXmlWrapper storage) throws Exception
	{
		if(runTypeCheck)
		{
			TypeChecker tc = new ModuleTypeChecker(moduleList);
			if (runTypeCheck)
			{
				typeCheckStarted();
				tc.typeCheck();
				moduleList.setLoaded(); // do not type check this classes again
			}
			if (TypeChecker.getErrorCount() == 0)
			{
				interpreter = new ModuleInterpreter(moduleList);

			} else
			{
				Writer typeErrors = new StringWriter();
				TypeChecker.printErrors(new PrintWriter(typeErrors));
				typeError(typeErrors.toString());
			}
		}else
			interpreter = new ModuleInterpreter(moduleList);
		
		interpreter.init(null);
		Module moduledef = interpreter.findModule(moduleName);
		
		if (moduledef == null)
		{
			error("Module not found: " + moduleName);
			throw new ClassNotFoundException(moduleName);
		}
		
		processTraces(moduledef,moduledef.defs ,moduleName, storage);
	}

	public void processTraces(Object classDef, List<Definition> definitions,
			String className, TraceXmlWrapper storage) throws Exception
	{
		try
		{
			

			if (storage != null)
				storage.StartClass(className);

			Integer numberOfTraces = 0;

			for (Object string : definitions)
			{
				if (string instanceof NamedTraceDefinition)
					numberOfTraces++;

			}
			processingClass(className, numberOfTraces);

			for (Object definition : definitions)
			{
				if (definition instanceof NamedTraceDefinition)
				{
					Context ctxt = createContext(classDef);

					evaluateTests(className, storage, definition, ctxt);
				}
			}

			completed();
		} catch (ContextException e)
		{

			error(e.getMessage());
			throw e;
		} catch (Exception e)
		{
			error(e.getMessage());
			throw e;
		} finally
		{
			if (storage != null)
				storage.Stop();
		}
	}

	private Context createContext(Object classdef) throws Exception
	{
		ObjectValue object = null;

		try
		{
			if (classdef instanceof ClassDefinition)
			{
				object = ((ClassDefinition) classdef).newInstance(null,
						null,
						interpreter.initialContext);
				Context ctxt = new ObjectContext(((ClassDefinition) classdef).name.location,
						((ClassDefinition) classdef).name.name + "()",
						interpreter.initialContext,
						object);
				return ctxt;
			}else
				if (classdef instanceof Module)
				{
					return interpreter.initialContext;
//					return new StateContext(
//							new LexLocation(), "global environment");
//					Environment env = new ModuleEnvironment((Module) classdef);
//					return  new StateContext(((Module) classdef).name.location,
//							"module scope",	null, ((Module) classdef).getStateContext());
//					//return ((Module)classdef).getStateContext();
//				return
//						new StateContext(((Module)classdef).name.location, "global environment");
				}
			
			error("Could not make content for: "+ classdef );
			throw new Exception("Could not make content for: "+ classdef );

		} catch (ValueException e)
		{
			error(e.getMessage());
			throw e;

		}
	}

	private void evaluateTests(String className, TraceXmlWrapper storage,
			Object traceDefinition, Context ctxt)
	{
		NamedTraceDefinition mtd = (NamedTraceDefinition) traceDefinition;
		TestSequence tests = mtd.getTests(ctxt);

		processingTrace(className, mtd.name.name, tests.size());
		if (storage != null)
			storage.StartTrace(mtd.name.name,
					mtd.location.file.getName(),
					mtd.location.startLine,
					mtd.location.startPos,

					tests.size());

		int n = 1;

		int faildCount = 0;
		int inconclusiveCount = 0;
		int skippedCount = 0;

		for (CallSequence test : tests)
		{
			processingTest(className, mtd.name.name, n);
			// Bodge until we figure out how to not have explicit op
			// names.
			String clean = test.toString().replaceAll("\\.\\w+`", ".");

			if (storage != null)
			{
				storage.StartTest(new Integer(n).toString(), clean);
				storage.StopElement();
			}

			if (test.getFilter() > 0)
			{
				skippedCount++;
				testFiltered(n, test.getFilter(), test);

				storage.AddSkippedResult(new Integer(n).toString());
			} else
			{
				interpreter.init(null); // Initialize completely between
				// every
				// run...
				List<Object> result = interpreter.runtrace(mtd.classDefinition, test);

				tests.filter(result, test, n);

				if (result.get(result.size() - 1) == Verdict.FAILED)
					faildCount++;

				else if (result.get(result.size() - 1) == Verdict.INCONCLUSIVE)
					inconclusiveCount++;

				if (storage != null)
				{
					storage.AddResults(new Integer(n).toString(), result);
				}

			}

			n++;
		}

		if (storage != null)
		{
			Verdict worstVerdict = Verdict.PASSED;
			if (faildCount > 0)
				worstVerdict = Verdict.FAILED;
			else if (inconclusiveCount > 0)
				worstVerdict = Verdict.INCONCLUSIVE;

			storage.AddTraceStatus(worstVerdict,
					tests.size(),
					skippedCount,
					faildCount,
					inconclusiveCount);
			storage.StopElement();
		}
	}

	private void processingClass(String className, Integer traceCount)
	{
		beginClass = System.currentTimeMillis();
		activeClass = className;
		System.out.println("Executing: " + className + " - Trace count: "
				+ traceCount);

		preProcessingClass(className, traceCount);
	}

	protected void preProcessingClass(String className, Integer traceCount)
	{

	}

	protected void processingTrace(String className, String traceName,
			Integer testCount)
	{
		printTraceStatus();
		beginTrace = System.currentTimeMillis();
		activeTrace = traceName;
		System.out.println(className + " - " + traceName + " Test count = "
				+ testCount);

		preProcessingTrace(className, traceName, testCount);
	}

	protected void preProcessingTrace(String className, String traceName,
			Integer testCount)
	{

	}

	protected void processingTest(String className, String traceName,
			Integer testNumber)
	{

	}

	protected void completed()
	{
		printTraceStatus();

		long endClass = System.currentTimeMillis();
		System.out.println("Class " + activeClass + " processed in "
				+ (double) (endClass - beginClass) / 1000 + " secs");

		preCompleted();
	}

	protected void preCompleted()
	{

	}

	protected void testFiltered(Integer number, Integer filteredBy,
			CallSequence test)
	{
		// Console.out.println("Test " + number + " = " + test);
		// Console.out.println("Test " + number + " FILTERED by test "
		// + filteredBy);
	}

	protected void error(String message)
	{
		System.err.println(message);
	}

	protected void typeError(String message)
	{
		System.err.println(message);
	}

	protected void parseError(String message)
	{
		System.err.println(message);
	}

	protected void typeCheckStarted()
	{

	}

	private void printTraceStatus()
	{
		if (activeTrace != null && beginTrace != 0)
		{
			long endTrace = System.currentTimeMillis();
			System.out.println("Trace " + activeClass + " - " + activeTrace
					+ " processed in " + (double) (endTrace - beginTrace)
					/ 1000 + " secs");
		}
		prePrintTraceStatus();
	}

	protected void prePrintTraceStatus()
	{

	}
}
