package it.unisa.ocelot.c.compiler;

import java.io.File;

public interface Compiler {
	public String preprocess(File pInput);
	public String preprocess(File pInput, File pOutput);
	public void compile(File pInput, File pOutput);
}
