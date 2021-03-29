package it.unisa.ocelot.c.instrumentor;

import it.unisa.ocelot.conf.ConfigManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.c.CStructure;
import org.eclipse.cdt.internal.core.dom.parser.c.CTypedef;

/**
 * Generates the macro that will contain the call. Besides, it removes all typedefs from the tree, putting them in the
 * "typedef" field.
 * @author simone
 *
 */
public class MacroDefinerVisitor extends ASTVisitor {
	private String callMacro;
	private String functionName;
	private String[] neededParameters;
	private List<IType> functionParameters;
	private Map<String, IParameter> parameters;
	
	private Map<String, IType> externalReferences;
	
	private ConfigManager config;
	
	public MacroDefinerVisitor(String pFunctionName, Map<String, IType> externalReferences) {
		this.shouldVisitDeclarations = true;
		this.shouldVisitDeclarators = true;
		this.shouldVisitTranslationUnit = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclSpecifiers = true;
		this.shouldVisitNames = true;
		
		this.functionName = pFunctionName;
		
		this.externalReferences = externalReferences;
		
		this.functionParameters = new ArrayList<IType>();
		
		this.parameters = new HashMap<String, IParameter>();
		
		new ArrayList<String>();
		new ArrayList<String>();
		
		this.callMacro = "";
		
		try {
			this.config = ConfigManager.getInstance();
		} catch (IOException e) {
			throw new RuntimeException("No config file found!");
		}
	}
	
	public List<IType> getFunctionParameters() {
		return functionParameters;
	}
	
	private IType getType(IType type) {
		while (type instanceof CTypedef || type instanceof CQualifierType) {
			if (type instanceof CTypedef) {
				CTypedef tdef = (CTypedef)type;
				
				type = tdef.getType();
			} else if (type instanceof CQualifierType) {
				CQualifierType qual = (CQualifierType)type;
				
				type = qual.getType();
			}
		}
		
		return type;
	}
	
	@Override
	public int visit(IASTDeclaration pDeclaration) {
		if (pDeclaration instanceof CASTFunctionDefinition) {
			CASTFunctionDefinition function = (CASTFunctionDefinition)pDeclaration;
			
			IASTName functionName;
			String[] parametersTypes;
			String[] parametersNames;
			if (function.getDeclarator() instanceof CASTFunctionDeclarator) {
				CASTFunctionDeclarator declarator = (CASTFunctionDeclarator)function.getDeclarator();
				
				functionName = declarator.getName();
				parametersTypes = new String[declarator.getParameters().length];
				parametersNames = new String[declarator.getParameters().length];
				
				for (int i = 0; i < declarator.getParameters().length; i++) {
					parametersTypes[i] = declarator.getParameters()[i].getDeclSpecifier().getRawSignature();
					parametersNames[i] = declarator.getParameters()[i].getDeclarator().getRawSignature().replaceAll("\\*\\s*", "");
				}
				
			} else if (function.getDeclarator() instanceof CASTKnRFunctionDeclarator) {
				CASTKnRFunctionDeclarator declarator = (CASTKnRFunctionDeclarator)function.getDeclarator();
				
				functionName = declarator.getName();
				parametersTypes = new String[declarator.getParameterNames().length];
				parametersNames = new String[declarator.getParameterNames().length];
				
				for (int i = 0; i < declarator.getParameterNames().length; i++) {
					IASTName paramName = declarator.getParameterNames()[i];
					IASTDeclarator declaration = declarator.getDeclaratorForParameterName(paramName);
					String type;
					if (declaration != null)
						type = ((IASTSimpleDeclaration)declaration.getParent()).getDeclSpecifier().getRawSignature();
					else
						type = "int";
					
					parametersNames[i] = paramName.getRawSignature().replaceAll("\\*\\s*", "");
					parametersTypes[i] = type;
				}
			} else {
				throw new RuntimeException("Unable to instrument this type of function: " + function.getDeclarator().getClass().toString());
			}
			
			//Goes on only if 
			if (!functionName.getRawSignature().equals(this.functionName))
				return PROCESS_CONTINUE;
			
			this.neededParameters = parametersNames;
		}
		
		return super.visit(pDeclaration);
	}
	
	/*
	 * NOTE:
	 * Assuming that all the structures are composed by either pointers or not pointers.
	 * If a field is a pointer, it will be assumed that this is of a C basic type.
	 * TODO Make sure that also in case of non basic C types this method works properly
	 * For example, Ocelot won't work if there is a structure with a field with type pointer
	 * to another struct, because it won't initialize the parameters of the pointed structure
	 */
	@Override
	public int leave(IASTDeclaration pDeclaration) {
		
		int result = super.leave(pDeclaration);
		if (pDeclaration instanceof CASTFunctionDefinition) {
			CASTFunctionDefinition function = (CASTFunctionDefinition)pDeclaration;
			
			IASTName functionName;
			String[] parametersStringTypes;
			String[] parametersStringNames;
			if (function.getDeclarator() instanceof CASTFunctionDeclarator) {
				CASTFunctionDeclarator declarator = (CASTFunctionDeclarator)function.getDeclarator();
				
				functionName = declarator.getName();
				parametersStringTypes = new String[declarator.getParameters().length];
				parametersStringNames = new String[declarator.getParameters().length];
				
				for (int i = 0; i < declarator.getParameters().length; i++) {
					parametersStringTypes[i] = declarator.getParameters()[i].getDeclSpecifier().getRawSignature();
					parametersStringNames[i] = declarator.getParameters()[i].getDeclarator().getRawSignature();
				}
				
			} else if (function.getDeclarator() instanceof CASTKnRFunctionDeclarator) {
				CASTKnRFunctionDeclarator declarator = (CASTKnRFunctionDeclarator)function.getDeclarator();
				
				functionName = declarator.getName();
				parametersStringTypes = new String[declarator.getParameterNames().length];
				parametersStringNames = new String[declarator.getParameterNames().length];
				
				for (int i = 0; i < declarator.getParameterNames().length; i++) {
					IASTName paramName = declarator.getParameterNames()[i];
					IASTDeclarator declaration = declarator.getDeclaratorForParameterName(paramName);
					String type;
					if (declaration != null)
						type = ((IASTSimpleDeclaration)declaration.getParent()).getDeclSpecifier().getRawSignature();
					else
						type = "int";
					
					parametersStringNames[i] = paramName.getRawSignature();
					parametersStringTypes[i] = type;
				}
			} else {
				throw new RuntimeException("Unable to instrument this type of function: " + function.getDeclarator().getClass().toString());
			}
			
			//Goes on only if 
			if (!functionName.getRawSignature().equals(this.functionName))
				return PROCESS_CONTINUE;

			for (String stringName : parametersStringNames) {
				if (stringName.contains("*")) {
				}
			}
			
			for (Entry<String, IType> type : this.externalReferences.entrySet()) {
				IType realType = getType(type.getValue());
				
				if (realType instanceof CPointerType) {
				}
			}
			
			String[] callParameters = new String[parametersStringTypes.length];
			String macro = "";
			
			macro += "#define OCELOT_CORES " + this.config.getThreads() + "\n";
			macro += "#define OCELOT_ARRAYS_SIZE " + this.config.getTestArraysSize() + "\n";
			macro += "#include \"CBridge.h\"\n";
			macro += "#define OCELOT_TYPES {";
			List<String> elements = new ArrayList<>();
			for (int i = 0; i < parametersStringTypes.length; i++) {
				String parameterStringName = parametersStringNames[i].replaceAll("\\*\\s*", "");
				IParameter parameterRealType = this.parameters.get(parameterStringName);
				IType type = getType(parameterRealType.getType());
				
				if (type instanceof CStructure) {
					//Handles structures with pointers
					VarStructTree tree = new VarStructTree("", (CStructure)type);
					List<StructNode> basics = tree.getBasicVariables();
					for (StructNode var : basics) {
//						this.functionParameters.add(var.type);
//						
						if (var.isPointer()) {
							System.err.println("We can't handle structures with pointers yet.");
//							String argcall = "OCELOT_NUM(OCELOT_ARGUMENT_POINTER(" + pointerArgument + "))";
//							macro += var.getCompleteName() + " = " + "&_v_ocelot_pointers[(int)(" + argcall + ")];\\\n";
//							pointerArgument++;
//							//TODO Test!
						}
					}
//					callParameters[outputArgument] = StringUtils.repeat('&', pointers) + "__arg" + outputArgument;
					
				} else {
					if (parametersStringNames[i].contains("*")) {
						IType realType = type;
						if (type instanceof CPointerType) {
							realType = getType(((CPointerType) type).getType());
						}
						
						if (realType instanceof CStructure) {
							//Just don't do anything...
						} else {
							String typeString = type.toString().replaceAll("\\*", "");
							typeString = typeString.replaceAll("const", "");
							typeString = typeString.replaceAll(" ", "");
							typeString = typeString.toUpperCase();
							
							elements.add("TYPE_"+ typeString);
						}
					}
				}
			}
			
			for (Entry<String, IType> type : this.externalReferences.entrySet()) {
				IType realType = getType(type.getValue());
				if (realType instanceof CPointerType) {
					String typeString = realType.toString().replaceAll("\\*", "");
					typeString = typeString.replaceAll("const", "");
					typeString = typeString.replaceAll(" ", "");
					typeString = typeString.toUpperCase();
					
					elements.add("TYPE_"+ typeString);
				}
			}
			
			
			macro += StringUtils.join(elements, ',');
			macro += "}\n";
			macro += "#define EXECUTE_OCELOT_TEST ";
			int outputArgument = 0;
			int inputArgument = 0;
			int pointerArgument = 0;
			
			for (int i = 0; i < parametersStringTypes.length; i++) {
				String parameterStringName = parametersStringNames[i].replaceAll("\\*\\s*", "");
				String parameterStringType = parametersStringTypes[i];
				
				IParameter parameterRealType = this.parameters.get(parameterStringName);				
				IType type = getType(parameterRealType.getType());
				IType realType = type;
				if (type instanceof CPointerType) {
					realType = getType(((CPointerType) type).getType());
				}
				int pointers = parametersStringNames[i].length() - parametersStringNames[i].replaceAll("\\*", "").length();
				
				if (type instanceof CStructure) {
					macro += parameterStringType; //Type
					macro += " __arg"+outputArgument+";\\\n";
					VarStructTree tree = new VarStructTree("__arg"+outputArgument, (CStructure)type);
					List<StructNode> basics = tree.getBasicVariables();
					for (StructNode var : basics) {
						this.functionParameters.add(var.type);
						String fieldType = var.type.toString().replaceAll("\\*\\s*", "");
						
						if (!var.isPointer()) {
							macro += fieldType;
							macro += " __str"+inputArgument;
							macro += " = (" + fieldType +")OCELOT_NUM(OCELOT_ARGUMENT_VALUE(" + inputArgument + "));\\\n"; //Assign
							macro += var.getCompleteName() + " = __str" + inputArgument + ";\\\n";
						
							inputArgument++;
						} else {
							String argcall = "OCELOT_NUM(OCELOT_ARGUMENT_POINTER(" + pointerArgument + "))";
							macro += var.getCompleteName() + " = " + "&_v_ocelot_pointers[(int)(" + argcall + ")];\\\n";
							pointerArgument++;
							//TODO Test!
						}
					}
					callParameters[outputArgument] = StringUtils.repeat('&', pointers) + "__arg" + outputArgument;
				} else if (realType instanceof CStructure) {
					macro += parameterStringType.replace("const", ""); //Type
					macro += " __arg"+outputArgument+";\\\n";
					VarStructTree tree = new VarStructTree("__arg"+outputArgument, (CStructure)realType);
					
					List<StructNode> basics = tree.getBasicVariables();
					for (StructNode var : basics) {
						this.functionParameters.add(var.type);
						String fieldType = var.type.toString().replaceAll("\\*\\s*", "");
						
						if (!var.isPointer()) {
							macro += fieldType;
							macro += " __str"+inputArgument;
							macro += " = (" + fieldType +")OCELOT_NUM(OCELOT_ARGUMENT_VALUE(" + inputArgument + "));\\\n"; //Assign
							macro += var.getCompleteNameWithPointers() + " = __str" + inputArgument + ";\\\n";
						
							inputArgument++;
						} else {
							String argcall = "OCELOT_NUM(OCELOT_ARGUMENT_POINTER(" + pointerArgument + "))";
							macro += var.getCompleteNameWithPointers() + " = " + "&_v_ocelot_pointers[(int)(" + argcall + ")];\\\n";
							pointerArgument++;
							//TODO Test!
						}
					}
					callParameters[outputArgument] = StringUtils.repeat('&', pointers) + "__arg" + outputArgument;
				} else {
					this.functionParameters.add(type);
					
					if (pointers == 0) {
						macro += parameterStringType;//Type
						macro += " __arg" +outputArgument; //Name
						macro += " = OCELOT_NUM(OCELOT_ARGUMENT_VALUE(" + inputArgument + "));\\\n"; //Assign
						
						callParameters[outputArgument] = StringUtils.repeat('&', pointers) + "__arg" + outputArgument;
						inputArgument++;
					} else {
						String argcall = "OCELOT_NUM(OCELOT_ARGUMENT_POINTER(" + pointerArgument + "))";
						callParameters[outputArgument] = StringUtils.repeat('&', pointers) + "_v_ocelot_pointers[(int)(" + argcall + ")]";
						pointerArgument++;
					}
				}
				outputArgument++;
			}
			
			for (Entry<String, IType> entry : this.externalReferences.entrySet()) {
				IType type = entry.getValue();
				IType realType = type;
				String variableName = entry.getKey();
				
				int pointers = 0;
				
				while (realType instanceof CPointerType) {
					realType = ((CPointerType)realType).getType();
					pointers++;
				}
				
				if (realType instanceof CStructure) {
					VarStructTree tree = new VarStructTree(variableName, (CStructure)type);
					List<StructNode> basics = tree.getBasicVariables();
					for (StructNode var : basics) {
						this.functionParameters.add(var.type);
						String fieldType = var.type.toString().replaceAll("\\*\\s*", "");
						
						if (!var.isPointer()) {
							macro += fieldType;
							macro += " __str"+inputArgument;
							macro += " = (" + fieldType +")OCELOT_NUM(OCELOT_ARGUMENT_VALUE(" + inputArgument + "));\\\n"; //Assign
							macro += var.getCompleteName() + " = __str" + inputArgument + ";\\\n";
						
							inputArgument++;
						} else {
							String argcall = "OCELOT_NUM(OCELOT_ARGUMENT_POINTER(" + pointerArgument + "))";
							macro += var.getCompleteName() + " = " + "&_v_ocelot_pointers[(int)(" + argcall + ")];\\\n";
							pointerArgument++;
							//TODO Test!
						}
					}
				} else {
					this.functionParameters.add(type);
					
					if (pointers == 0) {
						macro += variableName + " = OCELOT_NUM(OCELOT_ARGUMENT_VALUE(" + inputArgument + "));\\\n"; //Assign
						
						inputArgument++;
					} else {
						String argcall = "OCELOT_NUM(OCELOT_ARGUMENT_POINTER(" + pointerArgument + "))";
						macro += variableName + " = &_v_ocelot_pointers[(int)(" + argcall + ")];\\\n"; //Assign
						pointerArgument++;
					}
				}
			}
						
			macro += "OCELOT_TESTFUNCTION (" + StringUtils.join(callParameters, ",") + ");";
			
			this.callMacro = macro;
		}
		return result;
	}
	
	@Override
	public int visit(IASTStatement statement) {
		if (statement instanceof IASTCompoundStatement &&
				statement.getParent() instanceof CASTFunctionDefinition) {
			CASTFunctionDefinition function = (CASTFunctionDefinition)statement.getParent();
			if (function.getDeclarator().getName().getRawSignature().equals(this.functionName)) {
				for (String parameterName : this.neededParameters) {
					IBinding[] binds = ((IASTCompoundStatement) statement).getScope().find(parameterName);
					if (binds.length > 0)
						this.parameters.put(parameterName, (IParameter)binds[0]);
					else
						System.err.println("An error is going to occur. I couldn't find the parameter in the scope, sorry.");
				}
			}
		}
		
		return super.visit(statement);
	}
	
	@Override
	public int visit(IASTParameterDeclaration parameterDeclaration) {
		// TODO Auto-generated method stub
		return super.visit(parameterDeclaration);
	}
	
	@Override
	public int leave(IASTTranslationUnit tu) {
		return super.leave(tu);
	}
	
	public String getCallMacro() {
		return callMacro;
	}
}
