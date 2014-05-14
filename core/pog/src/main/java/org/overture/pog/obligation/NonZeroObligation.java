/*******************************************************************************
 *
 *	Copyright (C) 2008 Fujitsu Services Ltd.
 *
 *	Author: Nick Battle
 *
 *	This file is part of VDMJ.
 *
 *	VDMJ is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	VDMJ is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package org.overture.pog.obligation;

import org.overture.ast.expressions.AIntLiteralExp;
import org.overture.ast.expressions.ANotEqualBinaryExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstExpressionFactory;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.pog.pub.IPOContextStack;

public class NonZeroObligation extends ProofObligation
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5773921447005368923L;

	public NonZeroObligation(
		ILexLocation location, PExp exp, IPOContextStack ctxt)
	{
		super(exp, POType.NON_ZERO, ctxt, location);
		
		// exp <> 0
		
		AIntLiteralExp zeroExp = getIntLiteral(0);		
		ANotEqualBinaryExp notEqualsExp = AstExpressionFactory.newANotEqualBinaryExp(exp.clone(), zeroExp);
		
		
//		valuetree.setContext(ctxt.getContextNodeList());
		valuetree.setPredicate(ctxt.getPredWithContext(notEqualsExp));
	}
}
