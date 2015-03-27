package it.unisa.ocelot.c.makefile;

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
		return "System/Library/Frameworks/JavaVM.framework";
	}

	@Override
	public String getSystemInclude() {
		return "/usr/include";
	}

	@Override
	public String[] getGlib2Paths() {
		return new String[] {"/usr/lib/x86_64-linux-gnu/glib-2.0/include", "$(SYSTEM_INCLUDE)/glib-2.0"};
	}

	@Override
	public String[] getJavaPaths() {
		return new String[] {"$(JAVA_HOME)/Headers"};
	}

	@Override
	public String getCFlags() {
		return "-dynamiclib -framework JavaVM";
	}

	@Override
	public String getLibName() {
		return "libTest.jnilib";
	}
}
