package org.overture.ide.plugins.codegen.nodes;

import java.util.ArrayList;

import org.overture.ide.plugins.codegen.naming.TemplateParameters;
import org.overture.ide.plugins.codegen.visitor.CodeGenContext;

public class ClassCG implements ICommitable
{

	private String className;
	private String accessSpecifier;
	
	private ArrayList<ValueDefinitionCG> valueDefinitions;
	
	public ClassCG(String className, String accessSpecifier)
	{
		super();
		
		this.className = className;
		this.accessSpecifier = accessSpecifier;
		
		this.valueDefinitions = new ArrayList<>();
	}
		
	public String getClassName()
	{
		return className;
	}

	public void addValueDefinition(ValueDefinitionCG valueDef)
	{
		this.valueDefinitions.add(valueDef);
	}

	@Override
	public void commit(CodeGenContext context)
	{
		context.put(TemplateParameters.CLASS_NAME, className);
		context.put(TemplateParameters.CLASS_ACCESS_SPECIFIER, accessSpecifier);
		context.put(TemplateParameters.VALUE_DEFS, valueDefinitions);
	}
	
}