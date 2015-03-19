package parser;

import org.antlr.v4.runtime.TokenStream;

public class CodeFragment {
	public int from;
	public int to;
	public boolean jump;
	private String code;
	
	public CodeFragment(int from, int to, TokenStream pCode) {
		this.from = from;
		this.to = to;
		this.code = "";
		for (int i = from; i <= to; i++)
			this.code += pCode.get(i).getText() + " ";
		this.jump = false;
	}
	
	public String getCode() {
		return this.code;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CodeFragment))
			return false;
		
		CodeFragment fragment = (CodeFragment)obj;
		
		return (fragment.from == this.from && fragment.to == this.to);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.getCode();
	}
}
