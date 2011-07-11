package org.overture.ast.types.assistants;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.assistants.PDefinitionAssistant;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.AMapType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.AOptionalType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.ASeqType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.AUnknownType;
import org.overture.ast.types.EBasicType;
import org.overture.ast.types.EInvariantType;
import org.overture.ast.types.EType;
import org.overture.ast.types.PType;
import org.overture.ast.types.SBasicType;
import org.overture.ast.types.SInvariantType;
import org.overture.ast.types.SNumericBasicType;
import org.overture.runtime.Environment;
import org.overture.typecheck.TypeCheckInfo;
import org.overturetool.vdmj.lex.LexLocation;

public class PTypeAssistant {

	public static boolean hasSupertype(AClassType cto, AClassType other) {
		return PDefinitionAssistant.hasSupertype(cto.getClassdef(), other);
	}

	public static boolean isType(PType b, Class<? extends PType> typeclass) {
		return typeclass.isInstance(b);
	}

	

	public static PType getType(Set<PType> rtypes, LexLocation location) {
		// If there are any Optional(Unknowns) these are the result of
		// nil values, which set the overall type as optional. Other
		// optional types stay.

		Iterator<PType> tit = rtypes.iterator();
		boolean optional = false;

		while (tit.hasNext()) {
			PType t = tit.next();

			if (t instanceof AOptionalType) {
				AOptionalType ot = (AOptionalType) t;

				if (ot.getType() instanceof AUnknownType) {
					if (rtypes.size() > 1) {
						tit.remove();
						optional = true;
					} else {
						optional = false;
					}
				}
			}
		}

		assert rtypes.size() > 0 : "Getting type of empty TypeSet";
		PType result = null;

		if (rtypes.size() == 1) {
			result = rtypes.iterator().next();
		} else {
			result = new AUnionType(location, false,null, new ArrayList<PType>(
					rtypes));
		}

		return (optional ? new AOptionalType(location, false,null, result) : result);
	}

	public static boolean isUnknown(PType type) {
		return type.kindPType().equals(EType.UNKNOWN);// TODO: maybe needs some
														// more here
	}

	public static boolean isUnion(PType type) {
		return type.kindPType().equals(EType.UNION);
	}

	public static boolean isFunction(PType type) {
		return type.kindPType().equals(EType.FUNCTION);
	}

	public static AFunctionType getFunction(PType type) {
		switch (type.kindPType()) {
		case FUNCTION:
			if (type instanceof AFunctionType) {
				return (AFunctionType) type;
			}
			break;
		default:
			assert false : "Can't getFunction of a non-function";
		}
		return null;
	}

	public static PType typeResolve(PType type, Environment env,
			ATypeDefinition root,
			QuestionAnswerAdaptor<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) {

		PType result = null;

		switch (type.kindPType()) {
		case BASIC:
		case BRACKET:
		case CLASS:
			System.out.println("PTypeAssistent : typeResolve not implemented");
			break;
		case FUNCTION:
			if (type instanceof AFunctionType) {
				result = AFunctionTypeAssistent.typeResolve(
						(AFunctionType) type, env, root, rootVisitor, question);
			}
			break;
		case INMAP:
		case INVARIANT:
		case MAP:
		case OPERATION:
			if (type instanceof AOperationType) {
				result = AOperationTypeAssistant
						.typeResolve((AOperationType) type, env, root,
								rootVisitor, question);
			}
			break;
		case OPTIONAL:
		case PARAMETER:
		case PRODUCT:
		case QUOTE:
		case SEQ:
		case SEQ1:
		case SET:
		case UNDEFINED:
		case UNION:
		case UNKNOWN:
		case UNRESOLVED:
		case VOID:
		case VOIDRETURN:
			System.out.println("PTypeAssistent : typeResolve not implemented");
			break;
		default:
			type.setResolved(true);
			result = type;
			break;
		}
		return result;
	}

	public static void unResolve(PType type) {
		switch (type.kindPType()) {
		case BASIC:
		case BRACKET:
		case CLASS:
		case FUNCTION:
			if (type instanceof AFunctionType) {
				AFunctionTypeAssistent.unResolve((AFunctionType) type);
			}
			break;
		case INMAP:
		case INVARIANT:
		case MAP:
		case OPERATION:
			if (type instanceof AOperationType) {
				AOperationTypeAssistant.unResolve((AOperationType) type);
			}
			break;
		case OPTIONAL:
		case PARAMETER:
		case PRODUCT:
		case QUOTE:
		case SEQ:
		case SEQ1:
		case SET:
		case UNDEFINED:
		case UNION:
		case UNKNOWN:
		case UNRESOLVED:
		case VOID:
		case VOIDRETURN:
		default:
			System.out.println("PTypeAssistent : typeResolve not implemented");
			break;
		}

	}

	public static boolean isOperation(PType type) {
		switch (type.kindPType()) {
		case OPERATION:
			return true;
		case BASIC:
		case BRACKET:
		case CLASS:
		case FUNCTION:
		case INMAP:
		case INVARIANT:
		case MAP:
		case OPTIONAL:
		case PARAMETER:
		case PRODUCT:
		case QUOTE:
		case SEQ:
		case SEQ1:
		case SET:
		case UNDEFINED:
		case UNION:
		case UNKNOWN:
		case UNRESOLVED:
		case VOID:
		case VOIDRETURN:
		default:
			System.out.println("PTypeAssistent : typeResolve not implemented");
			return false;

		}
	}

	public static AOperationType getOperation(PType type) {
		switch (type.kindPType()) {
		case OPERATION:
			if (type instanceof AOperationType) {
				return (AOperationType) type;
			}

		default:
			assert false : "Can't getOperation of a non-operation";
			return null;

		}
	}

	public static boolean isSeq(PType type) {
		switch (type.kindPType()) {
		case SEQ:
		case SEQ1:
			return true;
		default:
			return false;
		}
	}

	public static ASeqType getSeq(PType type) {
		switch (type.kindPType()) {
		case SEQ:
		case SEQ1:
			if (type instanceof ASeqType) {
				return (ASeqType) type;
			}

		default:
			assert false : "Can't getSeq of a non-seq";
			return null;
		}
	}

	public static boolean isNumeric(PType type) {
		switch (type.kindPType()) {
		case BASIC:
			if (type instanceof SBasicType) {
				SBasicType bType = (SBasicType) type;
				if (bType.kindSBasicType() == EBasicType.NUMERIC) {
					return true;
				} else
					return false;

			}
		default:
			return false;
		}
	}

	public static boolean isMap(PType type) {
		switch (type.kindPType()) {
		case MAP:
			return true;
		default:
			return false;
		}
	}

	public static AMapType getMap(PType type) {
		switch (type.kindPType()) {
		case MAP:
			if (type instanceof AMapType) {
				return (AMapType) type;
			}
		default:
			assert false : "Can't getMap of a non-map";
			return null;
		}
	}

	public static boolean isSet(PType type) {
		switch (type.kindPType()) {
		case SET:
			return true;
		default:
			return false;
		}
	}

	public static ASetType getSet(PType type) {
		switch (type.kindPType()) {
		case SET:
			if (type instanceof ASetType) {
				return (ASetType) type;
			}
		default:
			assert false : "Can't getSet of a non-set";
			return null;
		}
	}

	public static SNumericBasicType getNumeric(PType type) {
		switch (type.kindPType()) {
		case BASIC:
			if (type instanceof SBasicType) {
				SBasicType bType = (SBasicType) type;
				if (bType.kindSBasicType() == EBasicType.NUMERIC) {
					if (type instanceof SNumericBasicType) {
						return (SNumericBasicType) type;
					}
				}
			}
		default:
			assert false : "Can't getNumeric of a non-numeric";
			return null;
		}
	}

	public static boolean isRecord(PType type) {
		switch (type.kindPType()) {
		case INVARIANT:
			if (type instanceof SInvariantType) {
				SInvariantType iType = (SInvariantType) type;
				if (iType.kindSInvariantType() == EInvariantType.RECORD) {
					if (type instanceof ARecordInvariantType) {
						return true;
					}
				}
			}
		default:
			return false;
		}
	}

	public static ARecordInvariantType getRecord(PType type) {
		switch (type.kindPType()) {
		case INVARIANT:
			if (type instanceof SInvariantType) {
				SInvariantType iType = (SInvariantType) type;
				if (iType.kindSInvariantType() == EInvariantType.RECORD) {
					if (type instanceof ARecordInvariantType) {
						return (ARecordInvariantType) type;
					}
				}
			}
		default:
			assert false : "Can't getRecord of a non-record";
			return null;
		}
	}

	public static boolean isClass(PType type) {
		switch (type.kindPType()) {
		case CLASS:
			if (type instanceof AClassType) {
				return true;
			}
		default:
			return false;
		}
	}

	public static AClassType getClassType(PType type) {
		switch (type.kindPType()) {
		case CLASS:
			if (type instanceof AClassType) {
				return (AClassType) type;
			}
		default:
			assert false : "Can't getClass of a non-class";
			return null;
		}

	}

	public static AProductType getProduct(PType type) {
		switch (type.kindPType()) {
		case PRODUCT:
			if (type instanceof AProductType) {
				return (AProductType) type;
			}
		default:
			assert false : "Can't getProduct of a non-product";
			return null;
		}
	}
	
	public static boolean isProduct(PType type) {
		switch (type.kindPType()) {
		case PRODUCT:
			if (type instanceof AProductType) {
				return true;
			}
		default:
			return false;
		}		
	}

}
