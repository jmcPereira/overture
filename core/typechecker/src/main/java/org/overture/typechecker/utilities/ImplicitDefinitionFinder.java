/*
 * #%~
 * The VDM Type Checker
 * %%
 * Copyright (C) 2008 - 2014 Overture
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #~%
 */
package org.overture.typechecker.utilities;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAdaptor;
import org.overture.ast.definitions.AClassInvariantDefinition;
import org.overture.ast.definitions.AEqualsDefinition;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ASystemClassDefinition;
import org.overture.ast.definitions.AThreadDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.AIntLiteralExp;
import org.overture.ast.expressions.ANewExp;
import org.overture.ast.expressions.ARealLiteralExp;
import org.overture.ast.expressions.AUndefinedExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.node.INode;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.AUnresolvedType;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.definition.ACpuClassDefinitionAssistantTC;

/**
 * This class implements a way to find ImplicitDefinitions from nodes from the AST.
 * 
 * @author kel
 */
public class ImplicitDefinitionFinder extends QuestionAdaptor<Environment>
{

	protected ITypeCheckerAssistantFactory af;

	public ImplicitDefinitionFinder(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	protected AStateDefinition findStateDefinition(Environment question,
			INode node)
	{
		return question.findStateDefinition();
	}

	@Override
	public void defaultSClassDefinition(SClassDefinition node,
			Environment question) throws AnalysisException
	{
		// TODO: should I expand this even more?
		if (node instanceof ASystemClassDefinition)
		{
			// af.createASystemClassDefinitionAssistant().implicitDefinitions((ASystemClassDefinition)node, question);

			af.createSClassDefinitionAssistant().implicitDefinitionsBase(node, question);

			for (PDefinition d : node.getDefinitions())
			{
				if (d instanceof AInstanceVariableDefinition)
				{
					AInstanceVariableDefinition iv = (AInstanceVariableDefinition) d;

					PType ivType = af.createPDefinitionAssistant().getType(iv);
					if (ivType instanceof AUnresolvedType
							&& iv.getExpression() instanceof AUndefinedExp)
					{
						AUnresolvedType ut = (AUnresolvedType) ivType;

						if (ut.getName().getFullName().equals("BUS"))
						{
							TypeCheckerErrors.warning(5014, "Uninitialized BUS ignored", d.getLocation(), d);
						}
					} else if (ivType instanceof AUnresolvedType
							&& iv.getExpression() instanceof ANewExp)
					{
						AUnresolvedType ut = (AUnresolvedType) ivType;

						if (ut.getName().getFullName().equals("CPU"))
						{
							ANewExp newExp = (ANewExp) iv.getExpression();
							PExp exp = newExp.getArgs().get(1);
							double speed = 0;
							if (exp instanceof AIntLiteralExp)
							{
								AIntLiteralExp frequencyExp = (AIntLiteralExp) newExp.getArgs().get(1);
								speed = frequencyExp.getValue().getValue();
							} else if (exp instanceof ARealLiteralExp)
							{
								ARealLiteralExp frequencyExp = (ARealLiteralExp) newExp.getArgs().get(1);
								speed = frequencyExp.getValue().getValue();
							}

							if (speed == 0)
							{
								TypeCheckerErrors.report(3305, "CPU frequency to slow: "
										+ speed + " Hz", d.getLocation(), d);
							} else if (speed > ACpuClassDefinitionAssistantTC.CPU_MAX_FREQUENCY)
							{
								TypeCheckerErrors.report(3306, "CPU frequency to fast: "
										+ speed + " Hz", d.getLocation(), d);
							}
						}
					}
				} else if (d instanceof AExplicitOperationDefinition)
				{
					AExplicitOperationDefinition edef = (AExplicitOperationDefinition) d;

					if (!edef.getName().getName().equals(node.getName().getName())
							|| !edef.getParameterPatterns().isEmpty())
					{
						TypeCheckerErrors.report(3285, "System class can only define a default constructor", d.getLocation(), d);
					}
				} else if (d instanceof AImplicitOperationDefinition)
				{
					AImplicitOperationDefinition idef = (AImplicitOperationDefinition) d;

					if (!d.getName().getName().equals(node.getName().getName()))
					{
						TypeCheckerErrors.report(3285, "System class can only define a default constructor", d.getLocation(), d);
					}

					if (idef.getBody() == null)
					{
						TypeCheckerErrors.report(3283, "System class constructor cannot be implicit", d.getLocation(), d);
					}
				} else
				{
					TypeCheckerErrors.report(3284, "System class can only define instance variables and a constructor", d.getLocation(), d);
				}
			}
		}
		{
			af.createSClassDefinitionAssistant().implicitDefinitionsBase(node, question);
		}
	}

	@Override
	public void caseAClassInvariantDefinition(AClassInvariantDefinition node,
			Environment question) throws AnalysisException
	{

	}

	@Override
	public void caseAEqualsDefinition(AEqualsDefinition node,
			Environment question) throws AnalysisException
	{

	}

	@Override
	public void caseAExplicitFunctionDefinition(
			AExplicitFunctionDefinition node, Environment question)
			throws AnalysisException
	{
		if (node.getPrecondition() != null)
		{
			node.setPredef(af.createAExplicitFunctionDefinitionAssistant().getPreDefinition(node));
			// PDefinitionAssistantTC.markUsed(d.getPredef());//ORIGINAL CODE
			af.getUsedMarker().caseAExplicitFunctionDefinition(node.getPredef());
		} else
		{
			node.setPredef(null);
		}

		if (node.getPostcondition() != null)
		{
			node.setPostdef(af.createAExplicitFunctionDefinitionAssistant().getPostDefinition(node));
			// PDefinitionAssistantTC.markUsed(d.getPostdef());//ORIGINAL CODE
			af.getUsedMarker().caseAExplicitFunctionDefinition(node.getPostdef());
		} else
		{
			node.setPostdef(null);
		}
	}

	@Override
	public void caseAExplicitOperationDefinition(
			AExplicitOperationDefinition node, Environment question)
			throws AnalysisException
	{
		node.setState(findStateDefinition(question, node));

		if (node.getPrecondition() != null)
		{
			node.setPredef(af.createAExplicitOperationDefinitionAssistant().getPreDefinition(node, question));
			af.createPDefinitionAssistant().markUsed(node.getPredef()); // ORIGINAL CODE
		}

		if (node.getPostcondition() != null)
		{
			node.setPostdef(af.createAExplicitOperationDefinitionAssistant().getPostDefinition(node, question));
			af.createPDefinitionAssistant().markUsed(node.getPostdef());
		}
	}

	@Override
	public void caseAImplicitFunctionDefinition(
			AImplicitFunctionDefinition node, Environment question)
			throws AnalysisException
	{

		if (node.getPrecondition() != null)
		{
			node.setPredef(af.createAImplicitFunctionDefinitionAssistant().getPreDefinition(node));
			af.createPDefinitionAssistant().markUsed(node.getPredef());
			// af.createPDefinitionAssistant().markUsed(node.getPredef());
		} else
		{
			node.setPredef(null);
		}

		if (node.getPostcondition() != null)
		{
			node.setPostdef(af.createAImplicitFunctionDefinitionAssistant().getPostDefinition(node));
			af.createPDefinitionAssistant().markUsed(node.getPostdef());
		} else
		{
			node.setPostdef(null);
		}
	}

	@Override
	public void caseAImplicitOperationDefinition(
			AImplicitOperationDefinition node, Environment question)
			throws AnalysisException
	{
		node.setState(findStateDefinition(question, node));

		if (node.getPrecondition() != null)
		{
			node.setPredef(af.createAImplicitOperationDefinitionAssistant().getPreDefinition(node, question));
			af.createPDefinitionAssistant().markUsed(node.getPredef());
		}

		if (node.getPostcondition() != null)
		{
			node.setPostdef(af.createAImplicitOperationDefinitionAssistant().getPostDefinition(node, question));
			af.createPDefinitionAssistant().markUsed(node.getPostdef());
		}
	}

	@Override
	public void caseAStateDefinition(AStateDefinition node, Environment question)
			throws AnalysisException
	{
		if (node.getInvPattern() != null)
		{
			node.setInvdef(af.createAStateDefinitionAssistant().getInvDefinition(node));
		}

		if (node.getInitPattern() != null)
		{
			node.setInitdef(af.createAStateDefinitionAssistant().getInitDefinition(node));
		}
	}

	@Override
	public void caseAThreadDefinition(AThreadDefinition node,
			Environment question) throws AnalysisException
	{
		// ORIGINAL CODE FROM ASSISTANT
		// node.setOperationDef(AThreadDefinitionAssistantTC.getThreadDefinition(node));
		// Mine non static call of the code.
		node.setOperationDef(af.createAThreadDefinitionAssistant().getThreadDefinition(node));
	}

	@Override
	public void caseATypeDefinition(ATypeDefinition node, Environment question)
			throws AnalysisException
	{
		if (node.getInvPattern() != null)
		{
			// node.setInvdef(getInvDefinition(d)); //Original code from Assistant.
			node.setInvdef(af.createATypeDefinitionAssistant().getInvDefinition(node));
			node.getInvType().setInvDef(node.getInvdef());
		} else
		{
			node.setInvdef(null);
		}

		if (node.getInvType() instanceof ANamedInvariantType)
		{
			ANamedInvariantType ntype = (ANamedInvariantType) node.getInvType();
			node.getComposeDefinitions().clear();

			for (PType compose : af.createPTypeAssistant().getComposeTypes(ntype.getType()))
			{
				ARecordInvariantType rtype = (ARecordInvariantType) compose;
				node.getComposeDefinitions().add(AstFactory.newATypeDefinition(rtype.getName(), rtype, null, null));
			}
		}
	}

	@Override
	public void defaultPDefinition(PDefinition node, Environment question)
			throws AnalysisException
	{
		return;
	}

}
