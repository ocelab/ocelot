package it.unisa.ocelot.c.makefile;

import java.io.IOException;

public class WindowsMakefileGenerator extends JNIMakefileGenerator {

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
		return new String[] { "C:/gtk/include/gtk-3.0", "C:/gtk/include/cairo",
				"C:/gtk/include/pango-1.0", "C:/gtk/include/atk-1.0",
				"C:/gtk/include/cairo", "C:/gtk/include/pixman-1",
				"C:/gtk/include", "C:/gtk/include/freetype2",
				"C:/gtk/include/gdk-pixbuf-2.0",
				"C:/gtk/include/libpng15", "C:/gtk/include/glib-2.0",
				"C:/gtk/lib/glib-2.0/include"};
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
}
