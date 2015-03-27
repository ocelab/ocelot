package it.unisa.ocelot.c.makefile;

public class LinuxMakefileGenerator extends JNIMakefileGenerator {
	public LinuxMakefileGenerator() {
		super("jni/makefile");
	}

	@Override
	public String getCCompiler() {
		return "gcc";
	}

	@Override
	public String getJavaHome() {
		return "/usr/lib/jvm/java-7-openjdk-amd64";
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
		return new String[] {"$(JAVA_HOME)/include", "$(JAVA_HOME)/include/linux"};
	}

	@Override
	public String getCFlags() {
		return "-shared -fpic";
	}

	@Override
	public String getLibName() {
		return "libTest.so";
	}
}
