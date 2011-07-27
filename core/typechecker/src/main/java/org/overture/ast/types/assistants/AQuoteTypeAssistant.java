package org.overture.ast.types.assistants;

import org.overture.ast.types.AQuoteType;
import org.overture.ast.types.PType;

public class AQuoteTypeAssistant {

	public static String toDisplay(AQuoteType exptype) {
		return "<" + exptype.getValue() + ">";
	}

	public static boolean equals(AQuoteType type, PType other) {
		other = PTypeAssistant.deBracket(other);

		if (other instanceof AQuoteType)
		{
			AQuoteType qother = (AQuoteType)other;
			return type.getValue().value.equals(qother.getValue().value);
		}

		return false;
	}

}
