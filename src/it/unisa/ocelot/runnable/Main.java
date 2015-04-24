package it.unisa.ocelot.runnable;
import it.unisa.ocelot.c.cfg.CFG;
import it.unisa.ocelot.c.cfg.CFGNode;
import it.unisa.ocelot.c.cfg.CFGVisitor;
import it.unisa.ocelot.c.cfg.LabeledEdge;
import it.unisa.ocelot.c.compiler.GCC;
import it.unisa.ocelot.c.instrumentor.InstrumentorVisitor;
import it.unisa.ocelot.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.*;

import javax.swing.JFrame;

import org.anarres.cpp.CppReader;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.Token;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.apache.commons.io.IOUtils;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.dom.rewrite.astwriter.ASTWriter;
import org.eclipse.core.runtime.CoreException;
import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;

import antlr_parser.CLexer;
import antlr_parser.CParser;
import antlr_parser.CodeFragment;
import antlr_parser.CParser.CompilationUnitContext;


public class Main extends JFrame {
	public static void main(String[] args) throws Exception {
		Main main1 = new Main("testobject/main.c", 1, 2);
			
		main1.setVisible(true);
	}
	
	public Graph graph;
	
	public Main(String pSourceFile) throws IOException {
		this.setSize(600, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		DirectedGraph<CodeFragment, antlr_parser.LabeledEdge> graph = new DefaultDirectedGraph<CodeFragment, antlr_parser.LabeledEdge>(antlr_parser.LabeledEdge.class);
		
		String source = Utils.readFile(pSourceFile);
		
		CharStream stream = new ANTLRInputStream(source);
		Lexer lexer = new CLexer(stream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		CParser parser = new CParser(tokenStream);
		//CVisitor<CodeFragment> instrumented = new InstrumentatorVisitor();
		antlr_parser.CFGVisitor cfgVisitor = new antlr_parser.CFGVisitor(graph, tokenStream);
		CompilationUnitContext tree = parser.compilationUnit();
		
		cfgVisitor.prepare();
		tree.accept(cfgVisitor);
		cfgVisitor.doFinalThings();
		
		JGraph jgraph = new JGraph(new JGraphModelAdapter<CodeFragment, antlr_parser.LabeledEdge>(graph) );
		
		jgraph.setPreferredSize(this.getSize());
		this.getContentPane().add(jgraph);
		
		this.graph = graph;
	}
	
	public Main(String pSourceFile, int second) throws Exception {
		this.setSize(600, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), pSourceFile);
		CFGVisitor visitor = new CFGVisitor(graph, "");
		
		translationUnit.accept(visitor);
		
		JGraph jgraph = new JGraph(new JGraphModelAdapter<CFGNode, LabeledEdge>(graph));
		
		jgraph.setPreferredSize(this.getSize());
		this.getContentPane().add(jgraph);
		
		this.setVisible(true);
		
		this.graph = graph;
	}
	
	public Main(String pSourceFile, int second, int third) throws Exception {
		String code = Utils.readFile(pSourceFile);
		CFG graph = new CFG();
		
		IASTTranslationUnit translationUnit = GCC.getTranslationUnit(code.toCharArray(), pSourceFile).copy();
		InstrumentorVisitor visitor = new InstrumentorVisitor();
		
		translationUnit.accept(visitor);
		
		ASTWriter writer = new ASTWriter();
		System.out.println(writer.write(translationUnit));
		System.exit(0);
		this.graph = graph;
	}
}
