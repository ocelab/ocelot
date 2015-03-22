package it.unisa.ocelot.runnable;
import it.unisa.ocelot.cfg.CFG;
import it.unisa.ocelot.cfg.CFGVisitor;
import it.unisa.ocelot.cfg.CFGNode;
import it.unisa.ocelot.cfg.LabeledEdge;
import it.unisa.ocelot.instrumentator.InstrumentatorVisitor;
import it.unisa.ocelot.util.Utils;

import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
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
		Main main1 = new Main(args[0], 1,2);
			
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
		
		IASTTranslationUnit translationUnit = Main.parse(code.toCharArray());
		CFGVisitor visitor = new CFGVisitor(graph);
		
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
		
		IASTTranslationUnit translationUnit = Main.parse(code.toCharArray());
		InstrumentatorVisitor visitor = new InstrumentatorVisitor();
		
		translationUnit.accept(visitor);
		
		System.out.println(translationUnit.getRawSignature());
				
		this.graph = graph;
	}
	
	private static IASTTranslationUnit parse(char[] code) throws Exception {
		FileContent fc = FileContent.create("/Path/ToResolveIncludePaths.cpp", code);
		Map<String, String> macroDefinitions = new HashMap<String, String>();
		String[] includeSearchPaths = new String[0];
		IScannerInfo si = new ScannerInfo(macroDefinitions, includeSearchPaths);
		IncludeFileContentProvider ifcp = IncludeFileContentProvider.getEmptyFilesProvider();
		IIndex idx = null;
		int options = ILanguage.OPTION_IS_SOURCE_UNIT;
		IParserLogService log = new DefaultLogService();
		
		return GCCLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
	}
}
