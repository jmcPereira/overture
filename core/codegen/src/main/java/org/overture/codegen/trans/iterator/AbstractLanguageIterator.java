/*
 * #%~
 * VDM Code Generator
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
package org.overture.codegen.trans.iterator;

import java.util.List;

import org.overture.codegen.cgast.SExpCG;
import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.expressions.AIdentifierVarExpCG;
import org.overture.codegen.cgast.patterns.AIdentifierPatternCG;
import org.overture.codegen.cgast.statements.AAssignmentStmCG;
import org.overture.codegen.ir.ITempVarGen;
import org.overture.codegen.trans.TempVarPrefixes;
import org.overture.codegen.trans.assistants.TransformationAssistantCG;

public abstract class AbstractLanguageIterator implements ILanguageIterator
{
	protected TransformationAssistantCG transformationAssistant;
	protected ITempVarGen tempGen;
	protected TempVarPrefixes varPrefixes;

	public AbstractLanguageIterator(
			TransformationAssistantCG transformationAssistant,
			ITempVarGen tempGen, TempVarPrefixes varPrefixes)
	{
		this.transformationAssistant = transformationAssistant;
		this.tempGen = tempGen;
		this.varPrefixes = varPrefixes;
	}

	@Override
	abstract public AVarLocalDeclCG getForLoopInit(AIdentifierVarExpCG setVar,
			List<AIdentifierPatternCG> ids, AIdentifierPatternCG id);

	@Override
	abstract public SExpCG getForLoopCond(AIdentifierVarExpCG setVar,
			List<AIdentifierPatternCG> ids, AIdentifierPatternCG id)
			throws AnalysisException;

	@Override
	abstract public SExpCG getForLoopInc(AIdentifierVarExpCG setVar,
			List<AIdentifierPatternCG> ids, AIdentifierPatternCG id);

	@Override
	abstract public AVarLocalDeclCG getNextElementDeclared(
			AIdentifierVarExpCG setVar, List<AIdentifierPatternCG> ids,
			AIdentifierPatternCG id) throws AnalysisException;

	@Override
	abstract public AAssignmentStmCG getNextElementAssigned(
			AIdentifierVarExpCG setVar, List<AIdentifierPatternCG> ids,
			AIdentifierPatternCG id) throws AnalysisException;
}
