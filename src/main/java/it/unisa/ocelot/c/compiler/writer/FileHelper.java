/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/


package it.unisa.ocelot.c.compiler.writer;


import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.internal.core.resources.ResourceLookup;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class FileHelper {

	@SuppressWarnings("unused")
	private static final String DEFAULT_LINE_DELIMITTER = "\n"; //$NON-NLS-1$

	public static IFile getIFilefromIASTNode(IASTNode node) {
		IPath implPath = new Path(node.getContainingFilename());
		return ResourceLookup.selectFileForLocation(implPath, null);
	}
	
	public static boolean isFirstWithinSecondLocation(IASTFileLocation loc1, IASTFileLocation loc2){
		
		boolean isEquals = true;
		
		isEquals &= loc1.getFileName().equals(loc2.getFileName());
		isEquals &= loc1.getNodeOffset() >= loc2.getNodeOffset();
		isEquals &= loc1.getNodeOffset()+loc1.getNodeLength() <= loc2.getNodeOffset() + loc2.getNodeLength();
		
		return isEquals;
	}
}
