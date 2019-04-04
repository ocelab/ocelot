package it.unisa.ocelot.experimentation;

import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.instrumentor.ExternalReferencesVisitor;
import it.unisa.ocelot.c.instrumentor.MacroDefinerVisitor;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTKnRFunctionDeclarator;
import org.eclipse.core.runtime.CoreException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CParser {
    private File cFile;

    public CParser (File cFile) {
        this.cFile = cFile;
    }

    public ArrayList<String> extractMethods () {
        ArrayList<String> functionsName = new ArrayList<>();
        IASTTranslationUnit translationUnit = null;
        try {
            translationUnit = GCC.getTranslationUnit(cFile.getAbsolutePath());

            IASTDeclaration [] declarations = translationUnit.getDeclarations();

            for (IASTDeclaration declaration : declarations) {
                String methodLocationPath = declaration.getFileLocation().getFileName();
                String methodLocationName = methodLocationPath.substring(methodLocationPath.lastIndexOf(File.separator)+1, methodLocationPath.length());

                //Take only methods written in the file)
                if (methodLocationName.equals(cFile.getName())) {
                    IASTName IASTFunctionName = visit(declaration);

                    if (IASTFunctionName != null) {
                        String functionName = String.valueOf(IASTFunctionName);
                        functionsName.add(functionName);
                    }
                }
            }
        } catch (NoClassDefFoundError e) {

        } catch (CoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return functionsName;
    }

    public IASTName visit(IASTDeclaration pDeclaration) {
        IASTName functionName = null;
        if (pDeclaration instanceof CASTFunctionDefinition) {
            CASTFunctionDefinition function = (CASTFunctionDefinition)pDeclaration;

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
        }



        return functionName;
    }
}
