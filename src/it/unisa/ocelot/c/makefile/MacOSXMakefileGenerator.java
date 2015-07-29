package it.unisa.ocelot.c.makefile;

import java.io.IOException;

public class MacOSXMakefileGenerator extends JNIMakefileGenerator {
	public MacOSXMakefileGenerator() {
		super("jni/makefile");
	}

	@Override
	public String getCCompiler() {
		return "gcc";
	}

	@Override
	public String getJavaHome() {
		return "/Library/Java/JavaVirtualMachines/jdk1.8.0_25.jdk/Contents/Home/include/";
	}

	@Override
	public String getSystemInclude() {
		return "/usr/include";
	}

	@Override
	public String[] getGlib2Paths() {
		return new String[] {"/usr/local/Cellar/glib/2.42.1/include/glib-2.0/", "/usr/local/Cellar/glib/2.42.1/lib/glib-2.0/include/"};
	}

	@Override
	public String[] getJavaPaths() {
		return new String[] {"$(JAVA_HOME)", "$(JAVA_HOME)darwin"};
	}

	@Override
	public String getCFlags() {
		return "-dynamiclib -framework JavaVM";
	}

	@Override
	public String getLibName() {
		return "libTest.jnilib";
	}
	
	@Override
	public Process runCompiler() throws IOException {
		return Runtime.getRuntime().exec(new String[] {"make", "--directory=jni"});
	}
}
