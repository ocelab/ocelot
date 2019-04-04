package it.unisa.ocelot.runnable.runners;

import it.unisa.ocelot.c.Builder;
import it.unisa.ocelot.c.BuildingException;
import it.unisa.ocelot.c.StandardBuilder;
import it.unisa.ocelot.c.makefile.DynamicJNIMakefileGenerator;
import it.unisa.ocelot.c.makefile.JNIMakefileGenerator;
import it.unisa.ocelot.conf.ConfigManager;
import it.unisa.ocelot.experimentation.CParser;
import it.unisa.ocelot.experimentation.project.CFile;
import it.unisa.ocelot.experimentation.project.Project;
import it.unisa.ocelot.util.Utils;
import org.eclipse.core.runtime.CoreException;

import java.io.*;
import java.util.*;

public class RunExperimantation {
    private static final String CONFIG_TAG = "_FUNCTION";
    private static final String TEST_TAG = CONFIG_TAG + "_TO_TEST";
    private static boolean EXECUTE_FUNCTIONLIST_GENERATOR = false;
    private static boolean FUNCTIONLIST_TOEXECUTE_GENERATOR = false;

    public void run () throws Exception {
        System.err.close();
        String experimentFolderPath = "experimentation_projects" + File.separator;

        if (FUNCTIONLIST_TOEXECUTE_GENERATOR) {
            //To execute only if there isn't txt with function informations
            if (EXECUTE_FUNCTIONLIST_GENERATOR) {
                ArrayList<Project> projects = getProjects(experimentFolderPath);

                for (Project project : projects) {
                    PrintWriter printWriter = new PrintWriter(project.getFileProject().getPath() + CONFIG_TAG + ".txt", "UTF-8");
                    for (CFile cFile : project.getcFiles()) {
                        String baseDir = cFile.getFile().getPath().substring(0, cFile.getFile().getPath().lastIndexOf(File.separator)+1);
                        String cName = cFile.getFile().getPath().substring(cFile.getFile().getPath().lastIndexOf(File.separator)+1, cFile.getFile().getPath().length());

                        for (String functionName : cFile.getFunctionNames()) {
                            if (functionName != null && !functionName.equals(""))
                                printWriter.write(baseDir + ":" + cName + ":" + functionName + "\n");
                        }
                    }

                    printWriter.close();
                }
            }


            File [] projectsTxt = new File(experimentFolderPath).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (file.isFile() && file.getName().contains(".txt"))
                        return true;
                    return false;
                }
            });

            boolean exception = false;
            for (int i = 0; i < projectsTxt.length; i++) {
                File projectTxt = projectsTxt[i];

                PrintWriter printWriter = new PrintWriter(projectTxt.getPath() + TEST_TAG + ".txt", "UTF-8");
                String [] txtUtils = Utils.readFile(projectTxt.getPath()).split("\n");

                int fileAnalized = 0;
                int totException = 0;

                System.out.println("\nProject: " + projectTxt.getName());
                System.out.print("File analized: " + fileAnalized + " of " + txtUtils.length);
                //for (String line : txtUtils) {
                for (int j = 0; j < txtUtils.length; j++) {
                    exception = false;

                    //PROBLEM
                    if (i == 1 && (j == 3157 || j >= 11888)) {
                        exception = true;
                        totException++;
                    } else {
                        String line = txtUtils[j];

                        String [] split = line.split(":");
                        String baseDir = split[0];
                        String testFilename = split[1];
                        String testFunction= split[2];

                        ConfigManager config = ConfigManager.getInstance();
                        config.setTestBasedir(baseDir);
                        config.setTestFileName(testFilename);
                        config.setTestFunction(testFunction);

                        try {
                            build();
                        } catch (Exception e) {
                            exception = true;
                            totException++;
                        }

                        if (!exception) {
                            printWriter.write(line + "\n");
                        }
                    }

                    System.out.print("\rFile analized: " + ++fileAnalized + " of " + txtUtils.length +
                            "\tException: " + totException + " of " + fileAnalized +
                            "\tNot Exception: " + (fileAnalized-totException) + " of " + fileAnalized);
                }

                printWriter.close();

                System.out.println();
            }
        }

        //Run multithread
    }

    public void build() throws IOException, BuildingException {
        ConfigManager config = ConfigManager.getInstance();

        Builder builder = new StandardBuilder(
                config.getTestFilename(),
                config.getTestFunction(),
                config.getTestIncludePaths());

        JNIMakefileGenerator generator = new DynamicJNIMakefileGenerator();

        for (String linkLibrary : config.getTestLink())
            generator.addLinkLibrary(linkLibrary);

        builder.setMakefileGenerator(generator);
        builder.setOutput(System.out);

        builder.build();
    }

    private static ArrayList<File> extractFileFromFolder (File file) {
        ArrayList<File> files = new ArrayList<File>();

        File [] tmpFiles = file.listFiles();

        for (File tmpFile : tmpFiles) {
            if (tmpFile.isDirectory()) {
                files.addAll(extractFileFromFolder(tmpFile));
            } else {
                if (tmpFile.isFile() && tmpFile.getName().contains(".c"))
                    files.add(tmpFile);
            }
        }

        return files;
    }

    private static ArrayList<Project> getProjects (String experimentFolderPath) {
        File experimentFolder = new File(experimentFolderPath);
        File [] experimentProjectsFolder = experimentFolder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    return true;
                return false;
            }
        });

        for (int i = 0; i < experimentProjectsFolder.length; i++) {
            if (experimentProjectsFolder[i].getName().contains("git")) {
                File tmp = experimentProjectsFolder[0];
                experimentProjectsFolder[0] = experimentProjectsFolder[i];
                experimentProjectsFolder[i] = tmp;
                break;
            }
        }

        int sizeToAnalize = 0;
        for (int i = 0; i < experimentProjectsFolder.length; i++) {
            ArrayList<File> cSourceFiles = extractFileFromFolder(experimentProjectsFolder[i]);
            sizeToAnalize += cSourceFiles.size();
        }

        int fileAnalized = 0;
        System.out.print("Analizing: " + ((float)fileAnalized/sizeToAnalize)*100 + "%");
        ArrayList<Project> projects = new ArrayList<>();
        for (int i = 0; i < experimentProjectsFolder.length; i++) {
            ArrayList<File> cSourceFiles = extractFileFromFolder(experimentProjectsFolder[i]);

            ArrayList<CFile> cFiles = new ArrayList<>();
            for (int j = 0; j < cSourceFiles.size(); j++) {
                CParser cParser = new CParser(cSourceFiles.get(j));
                ArrayList<String> functionsName = cParser.extractMethods();

                if (!functionsName.isEmpty()) {
                    cFiles.add(new CFile(cSourceFiles.get(j), functionsName));
                }

                fileAnalized++;
                System.out.print("\rAnalizing: " + ((float)fileAnalized/sizeToAnalize)*100 + "%");
            }

            projects.add(new Project(experimentProjectsFolder[i], cFiles));


        }

        return projects;
    }
}
