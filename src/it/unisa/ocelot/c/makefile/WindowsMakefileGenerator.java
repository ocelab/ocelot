package it.unisa.ocelot.c.makefile;

import java.io.IOException;

public class WindowsMakefileGenerator extends JNIMakefileGenerator {

	//private static String GTK_HOME = "C:/gtk/";
	private static String GTK_HOME = "C:/msys64/mingw64/";

	@Deprecated
	public WindowsMakefileGenerator() {
		super("jni/makefile");
	}

	@Override
	public String getCCompiler() {
		return "gcc";
	}

	@Override
	public String getJavaHome() {
		return "C:/'Program Files'/Java/jdk1.8.0_45";
	}

	@Override
	public String getSystemInclude() {
		return "";
	}

	@Override
	public String[] getGlib2Paths() {
		return new String[] { GTK_HOME + "include/gtk-3.0", GTK_HOME + "include/cairo",
				GTK_HOME + "include/pango-1.0", GTK_HOME + "include/atk-1.0",
				GTK_HOME + "include/cairo", GTK_HOME + "include/pixman-1",
				GTK_HOME + "include", GTK_HOME + "include/freetype2",
				GTK_HOME + "include/gdk-pixbuf-2.0",
				GTK_HOME + "include/libpng15", GTK_HOME + "include/glib-2.0",
				GTK_HOME + "lib/glib-2.0/include"};
	}

	@Override
	public String[] getJavaPaths() {
		return new String[] { "$(JAVA_HOME)/include",
				"$(JAVA_HOME)/include/win32" };
	}

	@Override
	public String getCFlags() {
		return "-shared -m64 -LC:/gtk/lib";
	}

	@Override
	public String getLibName() {
		return "Test.dll";
	}

	@Override
	public Process runCompiler() throws IOException {
		return Runtime.getRuntime().exec(new String[] {"mingw32-make", "--directory=jni"});
	}
	
	@Override
	public String getMoreOptions() {
		return "";
	}
}
