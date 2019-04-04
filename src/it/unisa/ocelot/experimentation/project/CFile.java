package it.unisa.ocelot.experimentation.project;

import java.io.File;
import java.util.ArrayList;

public class CFile {
    private String name;
    private File file;
    private ArrayList<String> functionNames;

    public CFile (File pFile, ArrayList<String> pFunctionNames) {
        this.name = pFile.getName();
        this.file = pFile;
        this.functionNames = pFunctionNames;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }

    public ArrayList<String> getFunctionNames() {
        return functionNames;
    }
}
