package it.unisa.ocelot.serverSocket;

import it.unisa.ocelot.c.BuildingException;
import it.unisa.ocelot.util.Utils;

import java.io.IOException;

public class SocketMakefileGenerator {
    private String fileName;
    private String os;

    public SocketMakefileGenerator () {
        this.fileName = "socket/makefile";
        this.os = System.getProperty("os.name");
    }

    public String getCCompiler () {
        return "gcc";
    }

    public String getExecName () {
        return "server";
    }

    public void generate () throws IOException {
        String result = "CC = " + getCCompiler() + "\n\n" +
                "SRCS = Event.c function.c graph.c main.c ocelot.c PointerMap.c server.c ServerSocket.c\n\n" +
                "SERVER_MAIN = ../" + getExecName() + "\n\n" +
                "MOREOPTS = -lm\n\n"+
                ".PHONY: clean all\n" +
                "all:\n"+
                "\t$(CC) -o $(SERVER_MAIN) $(SRCS) $(MOREOPTS)\n"+
                "clean:\n" +
                "\t$(RM) $(SERVER_MAIN)\n";

        Utils.writeFile(this.fileName, result);
    }

    public Process runCompiler() throws IOException, BuildingException {
        String makeCommand = "";

        if (os.contains("Win")) {
            makeCommand = "mingw32-make";
        } else if (os.contains("Mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            makeCommand = "make";
        } else {
            throw new BuildingException("Your operative system \"" + os + "\" is not supported");
        }

        return Runtime.getRuntime().exec(new String[] {makeCommand, "--directory=socket"});
    }
}
