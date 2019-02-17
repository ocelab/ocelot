package it.unisa.ocelot.c.makefile;

import java.io.IOException;

import it.unisa.ocelot.c.BuildingException;
import it.unisa.ocelot.conf.ConfigManager;

public class DynamicJNIMakefileGenerator extends JNIMakefileGenerator {

	private String os;
	
	public DynamicJNIMakefileGenerator() {
		super("jni/makefile");
		this.os = System.getProperty("os.name");
	}
	
	private String getLibraryNameFromOS() throws BuildingException {
		if (os.contains("Win"))
			return "Test.dll";
		else if (os.contains("Mac"))
			return "libTest.jnilib";
		else if (os.contains("nix") || os.contains("nux") || os.contains("aix"))
			return "libTest.so";
		else {
			throw new BuildingException("Your operative system \"" + os + "\" is not supported");
		}
	}

	@Override
	public String getCCompiler() {
		return "gcc";
	}

	@Override
	public String getJavaHome() {
		return System.getenv("JAVA_HOME");
	}

	@Override
	public String getSystemInclude() {
		String include = "";

		if (os.contains("nix") || os.contains("nux") || os.contains("aix"))
			include = "/usr/include";

		return include;
	}

	@Override
	public String[] getJavaPaths() throws BuildingException {
		String [] javaPaths = new String[2];
		javaPaths[0] = "$(JAVA_HOME)/include";

		if (os.contains("Win")) {
			javaPaths[1] = "$(JAVA_HOME)/include/win32";
		} else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
			javaPaths[1] = "$(JAVA_HOME)/include/linux";
		} else {
			throw new BuildingException("Your operative system \"" + os + "\" is not supported");
		}

		return javaPaths;
	}

	@Override
	public String getMoreOptions() {
		String moreOption = "";

		if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
			moreOption = "-lrt";
		}

		return moreOption;
	}

	@Override
	public String getCFlags() throws BuildingException {
		String cFlags = "";

		if (os.contains("Win")) {
			cFlags = "-shared -m64 -LC:/gtk/lib";
		} else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
			cFlags = "-shared -fpic";
		} else {
			throw new BuildingException("Your operative system \"" + os + "\" is not supported");
		}

		return cFlags;
	}

	@Override
	public String getLibName() throws BuildingException {
		return getLibraryNameFromOS();
	}

	@Override
	public Process runCompiler() throws IOException, BuildingException {
		String makeCommand = "";

		if (os.contains("Win")) {
			makeCommand = "mingw32-make";
		} else if (os.contains("Mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
			makeCommand = "make";
		} else {
			throw new BuildingException("Your operative system \"" + os + "\" is not supported");
		}

		return Runtime.getRuntime().exec(new String[] {makeCommand, "--directory=jni"});
	}

}
