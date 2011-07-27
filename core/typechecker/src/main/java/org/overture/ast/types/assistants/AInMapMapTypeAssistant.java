package org.overture.ast.types.assistants;

import org.overture.ast.types.AInMapMapType;

public class AInMapMapTypeAssistant {

	public static String toDisplay(AInMapMapType exptype) {
		return "inmap of (" + exptype.getFrom() + ") to (" + exptype.getTo() + ")";
	}

}
