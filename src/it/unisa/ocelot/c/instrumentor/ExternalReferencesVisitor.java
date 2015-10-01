package it.unisa.ocelot.c.instrumentor;

import it.unisa.ocelot.c.compiler.writer.ASTWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CPointerType;
import org.eclipse.cdt.internal.core.dom.parser.c.CTypedef;

/**
 * Generates the macro that will contain the call. Besides, it removes all typedefs from the tree, putting them in the
 * "typedef" field.
 * @author simone
 *
 */
public class ExternalReferencesVisitor extends ASTVisitor {
	private String functionName;
	
	private List<String> localVariables;
	private List<String> usedVariables;
	private Map<String, IASTDeclaration> declarations;
	
	private boolean visitingTheFunction;
		
	private IScope scope;
	
	public ExternalReferencesVisitor(String pFunctionName) {
		this.shouldVisitDeclarations = true;
		this.shouldVisitDeclarators = true;
		this.shouldVisitTranslationUnit = true;
		this.shouldVisitStatements = true;
		this.shouldVisitDeclSpecifiers = true;
		this.shouldVisitNames = true;
		
		this.functionName = pFunctionName;
		this.visitingTheFunction = false;
		
		this.localVariables = new ArrayList<String>();
		this.usedVariables = new ArrayList<String>();
	}
	
	private boolean isTheRightFunction(IASTDeclaration pDeclaration) {
		if (pDeclaration instanceof CASTFunctionDefinition) {
			CASTFunctionDefinition function = (CASTFunctionDefinition)pDeclaration;
			
			IASTName functionName;
			if (function.getDeclarator() instanceof CASTFunctionDeclarator) {
				CASTFunctionDeclarator declarator = (CASTFunctionDeclarator)function.getDeclarator();
				
				functionName = declarator.getName();
			} else if (function.getDeclarator() instanceof CASTKnRFunctionDeclarator) {
				CASTKnRFunctionDeclarator declarator = (CASTKnRFunctionDeclarator)function.getDeclarator();
				
				functionName = declarator.getName();
			} else {
				throw new RuntimeException("Unable to instrument this type of function: " + function.getDeclarator().getClass().toString());
			}
			
			//Goes on only if 
			if (!functionName.getRawSignature().equals(this.functionName))
				return false;
			else
				return true;
		}
		
		return false;
	}
	
	@Override
	public int visit(IASTDeclaration pDeclaration) {
		if (pDeclaration instanceof IASTFunctionDefinition) {
			if (!isTheRightFunction(pDeclaration)) 
				return PROCESS_SKIP;
			else {
				this.scope = ((IASTFunctionDefinition) pDeclaration).getScope();
				this.visitingTheFunction = true;
			}
		}
		
		return super.visit(pDeclaration);
	}
	
	@Override
	public int leave(IASTDeclaration pDeclaration) {
		if (pDeclaration instanceof IASTDeclaration) {
			if (this.isTheRightFunction(pDeclaration))
				this.visitingTheFunction = false;
		}
		
		return super.leave(pDeclaration);
	}
	
	
	@Override
	public int visit(IASTName name) {
		if (!this.visitingTheFunction)
			return super.visit(name);
		
		if (name.isDeclaration()) {
			this.localVariables.add(String.valueOf(name.getLookupKey()));
		} else
			this.usedVariables.add(String.valueOf(name.getLookupKey()));
		
		return super.visit(name);
	}
	
	public Map<String, IType> getExternalReferences() {
		List<String> externalVariables = new ArrayList<String>(this.usedVariables);
		externalVariables.removeAll(this.localVariables);
		
		Map<String, IType> types = new HashMap<>();
		for (String external : externalVariables) {
			IBinding[] bindings = this.scope.find(external);
			if (bindings.length > 0 && bindings[0] instanceof IVariable) {
				IVariable variable = (IVariable)bindings[0];
				
				types.put(external, variable.getType());
			}
		}
		
		return types;
	}
	
	private IType getType(IType type) {
		while (type instanceof CTypedef) {
			CTypedef tdef = (CTypedef)type;
			
			type = tdef.getType();
		}
		
		return type;
	}
	
	private String getStringType(IType type) {
		int pointers = 0;
		while (type instanceof CPointerType) {
			type = ((CPointerType)type).getType();
			pointers++;
		}
		
		return getType(type).toString() + StringUtils.repeat('*', pointers);
	}

	public String getExternalDeclarations() {
		String declarations = "";
		Map<String, IType> types = getExternalReferences();
		
		for (Entry<String, IType> entry : types.entrySet()) {
			String type = this.getStringType(entry.getValue());
			String name = entry.getKey();
						
			declarations += "extern " + type + " " + name + ";\n";
		}
		
		return declarations;
	}
}