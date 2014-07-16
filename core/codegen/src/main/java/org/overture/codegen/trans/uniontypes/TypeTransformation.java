package org.overture.codegen.trans.uniontypes;

import java.util.LinkedList;

import org.overture.codegen.cgast.PCG;
import org.overture.codegen.cgast.STypeCG;
import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.codegen.cgast.types.AIntNumericBasicTypeCG;
import org.overture.codegen.cgast.types.AQuoteTypeCG;
import org.overture.codegen.cgast.types.AUnionTypeCG;
import org.overture.codegen.ir.SourceNode;
import org.overture.codegen.trans.assistants.BaseTransformationAssistant;

public class TypeTransformation extends DepthFirstAnalysisAdaptor
{
	private BaseTransformationAssistant baseAssistant;

	public TypeTransformation(BaseTransformationAssistant baseAssistant)
	{
		this.baseAssistant = baseAssistant;
	}
	
	public AIntNumericBasicTypeCG consIntType(PCG node)
	{
		SourceNode sourceNode = node.getSourceNode();
		
		AIntNumericBasicTypeCG intType = new AIntNumericBasicTypeCG();
		intType.setSourceNode(sourceNode);
		
		return intType;
	}

	@Override
	public void caseAQuoteTypeCG(AQuoteTypeCG node) throws AnalysisException
	{
		baseAssistant.replaceNodeWith(node, consIntType(node));
	}
	
	@Override
	public void caseAUnionTypeCG(AUnionTypeCG node) throws AnalysisException
	{
		LinkedList<STypeCG> types = node.getTypes();

		for(STypeCG type : types)
		{
			type.apply(this);
		}
		
		boolean unionOfInts = true;
		
		for(STypeCG type : types)
		{
			if(!(type instanceof AIntNumericBasicTypeCG))
			{
				unionOfInts = false;
				break;
			}
		}
		
		if(unionOfInts)
		{
			baseAssistant.replaceNodeWith(node, consIntType(node));
		}
	}
}