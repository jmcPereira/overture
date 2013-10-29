package org.overture.codegen.vdmcodegen;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.codegen.utils.GeneratedData;
import org.overture.codegen.utils.GeneratedModule;
import org.overture.parser.util.ParserUtil;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.typechecker.util.TypeCheckerUtil;
import org.overture.typechecker.util.TypeCheckerUtil.TypeCheckResult;

public class CodeGenUtil
{
	//TODO: REMOVE NOT USED
	private static String getVelocityPropertiesPath(String relativePath)
	{
		String path = null;

		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL url = classLoader.getResource(relativePath);

		File file;
		try
		{
			if (url != null)
			{
				file = new File(url.toURI());
				path = file.getAbsolutePath();
			}
		} catch (Exception e)
		{
		}

		return path;
	}
	
	public static void generateOO(File file, GeneratedData data) throws AnalysisException
	{
		if (!file.exists() || !file.isFile())
		{
			throw new AnalysisException("Could not find file: "
					+ file.getAbsolutePath());
		}

		ParserResult<List<SClassDefinition>> parseResult = ParserUtil.parseOo(file);

		if (parseResult.errors.size() > 0)
		{
			throw new AnalysisException("File did not parse: "
					+ file.getAbsolutePath());
		}

		TypeCheckResult<List<SClassDefinition>> typeCheckResult = TypeCheckerUtil.typeCheckPp(file);

		if (typeCheckResult.errors.size() > 0)
		{
			throw new AnalysisException("File did not pass the type check: "
					+ file.getName());
		}

		CodeGen vdmCodGen = new CodeGen();
		try
		{
			vdmCodGen.generateCode(typeCheckResult.result, data);
			vdmCodGen.generateQuotes(data);
		} catch (AnalysisException e)
		{
			throw new AnalysisException("Unable to generate code from specification. Exception message: "
					+ e.getMessage());
		}
	}

	public static GeneratedData generateOO(String[] args) throws AnalysisException
	{		
		GeneratedData data = new GeneratedData();
		
		for (int i = 1; i < args.length; i++)
		{
			String fileName = args[i];
			File file = new File(fileName);
			generateOO(file, data);
		}
		
		return data;
	}

	public static String generateFromExp(String exp) throws AnalysisException
	{
		if (exp == null || exp.isEmpty())
		{
			throw new AnalysisException("No expression to generate from");
		}

		ParserResult<PExp> parseResult = null;
		try
		{
			parseResult = ParserUtil.parseExpression(exp);
		} catch (Exception e)
		{
			throw new AnalysisException("Unable to parse expression: " + exp
					+ ". Message: " + e.getMessage());
		}

		if (parseResult.errors.size() > 0)
		{
			throw new AnalysisException("Unable to parse expression: " + exp);
		}

		TypeCheckResult<PExp> typeCheckResult = null;
		try
		{
			typeCheckResult = TypeCheckerUtil.typeCheckExpression(exp);
		} catch (Exception e)
		{
			throw new AnalysisException("Unable to type check expression: "
					+ exp + ". Message: " + e.getMessage());
		}
		
		

		if (typeCheckResult.errors.size() > 0)
		{
			throw new AnalysisException("Unable to type check expression: "
					+ exp);
		}

		CodeGen vdmCodGen = new CodeGen();
		try
		{
			return vdmCodGen.generateCode(typeCheckResult.result);

		} catch (AnalysisException e)
		{
			throw new AnalysisException("Unable to generate code from expression: "
					+ exp + ". Exception message: " + e.getMessage());
		}

	}
	
	public static void generateSourceFiles(List<GeneratedModule> classes)
	{
		CodeGen vdmCodGen = new CodeGen();
		vdmCodGen.generateSourceFiles(classes);
	}
	
	public static void generateCodeGenUtils()
	{
		CodeGen vdmCodGen = new CodeGen();
		vdmCodGen.generateCodeGenUtils();
	}
	
}