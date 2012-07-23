package org.overture.ide.plugins.uml2.uml2vdm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.AClassClassDefinition;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.expressions.AIntLiteralExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.lex.Dialect;
import org.overture.ast.lex.LexIntegerToken;
import org.overture.ast.lex.LexLocation;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.PType;
import org.overture.config.Settings;
import org.overture.parser.lex.LexException;
import org.overture.parser.syntax.ParserException;
import org.overture.parser.util.ParserUtil;
import org.overture.parser.util.ParserUtil.ParserResult;
import org.overture.prettyprinter.PrettyPrinterEnv;
import org.overture.prettyprinter.PrettyPrinterVisitor;

public class Uml2Vdm
{
	final static LexLocation location = new LexLocation(new File("generated"), "generating", 0, 0, 0, 0, 0, 0);
	private static final AIntLiteralExp NEW_A_INT_ZERRO_LITERAL_EXP = AstFactory.newAIntLiteralExp(new LexIntegerToken(0, location));
	Model model;
	private String extension = "vdmpp";
	private VdmTypeCreator tc = new VdmTypeCreator();

	public Uml2Vdm(URI uri)
	{

		Resource resource = new ResourceSetImpl().getResource(uri, true);
		for (EObject c : resource.getContents())
		{
			if (c instanceof Model)
			{
				model = (Model) c;
			}
		}

	}

	public void convert(File outputDir)
	{
		if (model != null)
		{
			Map<String, String> classes = new HashMap<String, String>();
			for (Element e : model.getOwnedElements())
			{
				if (e instanceof Class)
				{
					Class class_ = (Class) e;
					System.out.println("Converting: " + class_.getName());
					try
					{
						classes.put(class_.getName(), createClass(class_).apply(new PrettyPrinterVisitor(), new PrettyPrinterEnv()));
					} catch (AnalysisException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}

			for (Entry<String, String> c : classes.entrySet())
			{
				writeClassFile(outputDir, c);
			}
		}
	}

	private void writeClassFile(File outputDir, Entry<String, String> c)
	{
		try
		{
			outputDir.mkdirs();
			FileWriter outFile = new FileWriter(new File(outputDir, c.getKey()
					+ "." + extension));
			PrintWriter out = new PrintWriter(outFile);

			out.println(c.getValue());
			out.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private INode createClass(Class class_)
	{
		AClassClassDefinition c = AstFactory.newAClassClassDefinition();
		c.setName(new LexNameToken(class_.getName(), class_.getName(), null));

		for (Property att : class_.getOwnedAttributes())
		{
			if (att.isReadOnly())
			{
				createValue(c, att);
			} else
			{
				createInstanceVar(c, att);
			}
		}

		for (Operation op : class_.getOwnedOperations())
		{
			if (op.isQuery())
			{
				createFunction(c, op);
			} else
			{
				createOperation(c, op);
			}
		}

		return c;
	}

	private void createFunction(AClassClassDefinition c, Operation op)
	{
		LexNameToken name = new LexNameToken(c.getName().name, op.getName(), null);

		List<List<PPattern>> paramPatternList = new Vector<List<PPattern>>();
		List<PPattern> paramPatterns = new Vector<PPattern>();
		paramPatternList.add(paramPatterns);
		List<PType> parameterTypes = new Vector<PType>();

		for (Parameter p : op.getOwnedParameters())
		{
			if (p.getName() == null)
			{
				continue;// this is the return type
			}
			parameterTypes.add(tc.convert(p.getType()));
			paramPatterns.add(AstFactory.newAIdentifierPattern(new LexNameToken(c.getName().name, p.getName(), location)));
		}

		AFunctionType type = AstFactory.newAFunctionType(null, true, parameterTypes, tc.convert(op.getType()));

		AExplicitFunctionDefinition operation = AstFactory.newAExplicitFunctionDefinition(name, null, null, type, paramPatternList, AstFactory.newAUndefinedExp(null), null, null, false, null);
		c.getDefinitions().add(operation);
	}

	private void createOperation(AClassClassDefinition c, Operation op)
	{
		LexNameToken name = new LexNameToken(c.getName().name, op.getName(), null);
		List<PType> parameterTypes = new Vector<PType>();
		List<PPattern> parameters = new Vector<PPattern>();
		for (Parameter p : op.getOwnedParameters())
		{
			if (p.getName() == null)
			{
				continue;// this is the return type
			}
			parameterTypes.add(tc.convert(p.getType()));
			parameters.add(AstFactory.newAIdentifierPattern(new LexNameToken(c.getName().name, p.getName(), location)));
		}
		AOperationType type = AstFactory.newAOperationType(null, parameterTypes, tc.convert(op.getType()));

		AExplicitOperationDefinition operation = AstFactory.newAExplicitOperationDefinition(name, type, parameters, null, null, AstFactory.newANotYetSpecifiedStm(null));
		c.getDefinitions().add(operation);
	}

	private void createInstanceVar(AClassClassDefinition c, Property att)
	{
		PType type = tc.convert(att.getType());
		PExp defaultExp = NEW_A_INT_ZERRO_LITERAL_EXP.clone();
		if (att.getDefault() != null && !att.getDefault().isEmpty())
		{
			Settings.dialect = Dialect.VDM_SL;
			ParserResult<PExp> resExp = null;
			boolean failed = false;
			try
			{
				resExp = ParserUtil.parseExpression(att.getDefault());
			} catch (ParserException e)
			{
				failed = true;
				e.printStackTrace();
			} catch (LexException e)
			{
				failed = true;
				e.printStackTrace();
			}
			if (resExp.errors.isEmpty() && !failed)
			{
				defaultExp = resExp.result;
			}else
			{
				System.out.println("Faild to parse expression for attribute: "+att.getName()+ " in class "+ c.getName().name+ " default is: "+ att.getDefault());
			}
		}
		AInstanceVariableDefinition inst = AstFactory.newAInstanceVariableDefinition(new LexNameToken(c.getName().name, att.getName(), location), type, defaultExp);
		c.getDefinitions().add(inst);
	}

	private void createValue(AClassClassDefinition c, Property att)
	{
		PType type = tc.convert(att.getType());
		
		
		PExp defaultExp = NEW_A_INT_ZERRO_LITERAL_EXP.clone();
		if (att.getDefault() != null && !att.getDefault().isEmpty())
		{
			Settings.dialect = Dialect.VDM_SL;
			ParserResult<PExp> resExp = null;
			boolean failed = false;
			try
			{
				resExp = ParserUtil.parseExpression(att.getDefault());
			} catch (ParserException e)
			{
				failed = true;
				e.printStackTrace();
			} catch (LexException e)
			{
				failed = true;
				e.printStackTrace();
			}
			if (resExp.errors.isEmpty() && !failed)
			{
				defaultExp = resExp.result;
			}else
			{
				System.out.println("Faild to parse expression for attribute: "+att.getName()+ " in class "+ c.getName().name+ " default is: "+ att.getDefault());
			}
		}
		
		AValueDefinition inst = AstFactory.newAValueDefinition(AstFactory.newAIdentifierPattern(new LexNameToken(c.getName().name, att.getName(), location)), null, type, defaultExp);
		c.getDefinitions().add(inst);
	}
}