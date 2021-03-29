package it.unisa.ocelot.c.makefile;

import java.io.IOException;

import it.unisa.ocelot.c.BuildingException;
import it.unisa.ocelot.conf.ConfigManager;

public class DynamicMakefileGenerator extends JNIMakefileGenerator {

	private ConfigManager config;
	private String libName;
	
	public DynamicMakefileGenerator(ConfigManager config) {
		super("jni/makefile");
		this.config = config;

		try {
			this.libName = this.getLibraryNameFromOS();
		} catch (BuildingException e) {
			e.printStackTrace();
		}
	}
	
	private String getLibraryNameFromOS() throws BuildingException {
		String os = System.getProperty("os.name");
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
		return this.config.getJavaHome();
	}

	@Override
	public String getSystemInclude() {
		return this.config.getSystemInclude();
	}

	@Override
	public String[] getGlib2Paths() {
		return this.config.getGlib2Paths();
	}

	@Override
	public String[] getJavaPaths() {
		return this.config.getJavaPaths();
	}

	@Override
	public String getMoreOptions() {
		return this.config.getMoreOptions();
	}

	@Override
	public String getCFlags() {
		return this.config.getCFlags();
	}

	@Override
	public String getLibName() {
		return this.libName;
	}

	@Override
	public Process runCompiler() throws IOException {
		return Runtime.getRuntime().exec(new String[] {this.config.getMakeCommand(), "--directory=jni"});
	}

}
