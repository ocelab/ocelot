package it.unisa.ocelot.experimentation.project;

import java.io.File;
import java.util.ArrayList;

public class Project {
    private String nameProject;
    private File fileProject;
    private ArrayList<CFile> cFiles;

    public Project (File pFile, ArrayList<CFile> pCFiles) {
        this.nameProject = pFile.getName();
        this.fileProject = pFile;
        this.cFiles = pCFiles;
    }

    public String getNameProject() {
        return nameProject;
    }

    public File getFileProject() {
        return fileProject;
    }

    public ArrayList<CFile> getcFiles() {
        return cFiles;
    }
}
