import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;

import parser.CFGNode;
import parser.CFGVisitor;
import parser.CLexer;
import parser.CParser;
import parser.CVisitor;
import parser.CodeFragment;
import parser.CDT_CFGVisitor;
import parser.CParser.CompilationUnitContext;
import util.LabeledEdge;
import util.Utils;

public class Main extends JFrame {
	public static void main(String[] args) throws Exception {
		Main main = new Main(args[0], 1);
		main.setVisible(true);
	}
	
	public Main(String pSourceFile) throws IOException {
		this.setSize(600, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		DirectedGraph<CodeFragment, LabeledEdge> graph = new DefaultDirectedGraph<CodeFragment, LabeledEdge>(LabeledEdge.class);
		
		String source = Utils.readFile(pSourceFile);
		
		CharStream stream = new ANTLRInputStream(source);
		Lexer lexer = new CLexer(stream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		CParser parser = new CParser(tokenStream);
		//CVisitor<CodeFragment> instrumented = new InstrumentatorVisitor();
		CFGVisitor cfgVisitor = new CFGVisitor(graph, tokenStream);
		CompilationUnitContext tree = parser.compilationUnit();
		
		cfgVisitor.prepare();
		tree.accept(cfgVisitor);
		cfgVisitor.doFinalThings();
		
		JGraph jgraph = new JGraph(new JGraphModelAdapter<CodeFragment, LabeledEdge>(graph) );
		
		jgraph.setPreferredSize(this.getSize());
		this.getContentPane().add(jgraph);
	}
	
	public Main(String pSourceFile, int second) throws Exception {
		this.setSize(600, 600);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		String code = Utils.readFile(pSourceFile);
		DirectedGraph<CFGNode, LabeledEdge> graph = new DefaultDirectedGraph<CFGNode, LabeledEdge>(LabeledEdge.class);
		
		IASTTranslationUnit translationUnit = Main.parse(code.toCharArray());
		CDT_CFGVisitor visitor = new CDT_CFGVisitor(graph);
		
		visitor.shouldVisitStatements = true;
		visitor.prepare();
		translationUnit.accept(visitor);
		visitor.doFinalThings();
		
		JGraph jgraph = new JGraph(new JGraphModelAdapter<CFGNode, LabeledEdge>(graph));
		
		jgraph.setPreferredSize(this.getSize());
		this.getContentPane().add(jgraph);
	}
	
	
	public static void dmain(String[] args) throws Exception {
		String code = Utils.readFile(args[0]);
		DirectedGraph<CFGNode, LabeledEdge> graph = new DefaultDirectedGraph<CFGNode, LabeledEdge>(LabeledEdge.class);
		
		IASTTranslationUnit translationUnit = parse(code.toCharArray());
		ASTVisitor visitor = new CDT_CFGVisitor(graph);
		
		visitor.shouldVisitStatements = true;
		translationUnit.accept(visitor);
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
		return GPPLanguage.getDefault().getASTTranslationUnit(fc, si, ifcp, idx, options, log);
	}
}
