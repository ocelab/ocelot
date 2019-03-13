package it.unisa.ocelot.c.instrumentor;

import it.unisa.ocelot.c.types.*;
import it.unisa.ocelot.conf.ConfigManager;

import java.io.IOException;
import java.util.*;
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
	private LinkedHashMap<String, IType> functionParametersMap;
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
		this.functionParametersMap = new LinkedHashMap<>();
		
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

	public CType[] getFunctionParametersFromMacroDefinerVisitor (LinkedHashMap<String, IType> functionParametersMap, String nameOfStruct, String typeDefName) {
		CType [] parameters = new CType[functionParametersMap.size()];
		int i = 0;

		for (Map.Entry<String, IType> entry : functionParametersMap.entrySet()) {
			CType cType = null;

			String nameOfVariable = entry.getKey();
			IType type = entry.getValue();

			String typedefName = typeDefName;

			if (type instanceof CTypedef) {
				typedefName = ((CTypedef) type).getName();
				type = ((CTypedef) type).getType();
			}

			int numberOfPointer = 0;
			if (type instanceof CPointerType) {
				while (type instanceof CPointerType) {
					numberOfPointer++;
					type = ((CPointerType) type).getType();
				}

				if (type instanceof CTypedef) {
					CTypedef typeDef = (CTypedef)type;
					typedefName = typeDef.getName();
				}

				type = getType(type);
			}


			if (type instanceof CStructure) {
				/*
				Name of struct == null -> first structure
				Name of struct != null -> method called from structure
				!type.toString().equals(nameOfStruct) -> struct different
				 */
				if (nameOfStruct == null || !type.toString().equals(nameOfStruct)) {
					VarStructTree tree = new VarStructTree(type.toString(), (CStructure)type);;
					List<StructNode> basics = tree.getBasicVariables();

					LinkedHashMap<String, IType> structTypeVariablesMap = new LinkedHashMap<>();
					for (StructNode structNode : basics) {
						structTypeVariablesMap.put(structNode.name, structNode.type);
					}

					CType [] structParameters = getFunctionParametersFromMacroDefinerVisitor(structTypeVariablesMap, type.toString(), typedefName);
					cType = new CStruct(nameOfVariable, type.toString(), typedefName, structParameters);
				} else {
					cType = new CStruct(nameOfVariable, type.toString(), typedefName);
				}
			}

			else {
				if (type.toString().equals("char")) {
					cType = new CChar(nameOfVariable);
				}

				else if (type.toString().equals("int")) {
					cType = new CInteger(nameOfVariable);
				}

				else if (type.toString().equals("double")) {
					cType = new CDouble(nameOfVariable);
				}

				else if (type.toString().equals("float")) {
					cType = new CFloat(nameOfVariable);
				}
			}

			//Pointer checker
			while (numberOfPointer-- > 0) {
				cType = new CPointer(cType);
			}


			parameters[i++] = cType;
		}


		return parameters;
	}

	public List<IType> getFunctionParameters() {
		return functionParameters;
	}

	public LinkedHashMap<String, IType> getFunctionParametersMap() {
		return functionParametersMap;
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


			for (int i = 0; i < parametersStringNames.length; i++) {
				String parameterStringName = parametersStringNames[i].replaceAll("\\*\\s*", "");

				IParameter parameterRealType = this.parameters.get(parameterStringName);
				IType type = getType(parameterRealType.getType());

				this.functionParameters.add(type);
				this.functionParametersMap.put(parameterStringName, type);
			}

			
			for (Entry<String, IType> entry : this.externalReferences.entrySet()) {
				IType type = entry.getValue();
				IType realType = type;
				String variableName = entry.getKey();
				
				while (realType instanceof CPointerType) {
					realType = ((CPointerType)realType).getType();
				}
				
				if (realType instanceof CStructure) {
					VarStructTree tree = new VarStructTree(variableName, (CStructure)type);
					List<StructNode> basics = tree.getBasicVariables();
					for (StructNode var : basics) {
						this.functionParameters.add(var.type);
					}
				} else {
					this.functionParameters.add(type);
				}
			}
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
