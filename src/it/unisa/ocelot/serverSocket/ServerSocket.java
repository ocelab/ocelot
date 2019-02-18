package it.unisa.ocelot.serverSocket;

import java.io.IOException;
import java.lang.reflect.Field;

public class ServerSocket {
    private String serverSocketPath;
    private String serverProgramName;

    public ServerSocket () {
        this.serverSocketPath = "socket";
        this.serverProgramName = "serverSocket";
    }

    public int compile () {
        int result = -1;

        SocketMakefileGenerator socketMakefileGenerator = new SocketMakefileGenerator();
        try {
            socketMakefileGenerator.generate();
            Process proc = socketMakefileGenerator.runCompiler();
            result = proc.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public int startServer () {
        int pid = -1;
        String command = "./server";

        try {
            System.out.println("\nStarting server...");
            Process proc = Runtime.getRuntime().exec(command);
            Thread.sleep(100);
            if (proc.isAlive()) {
                System.out.println("Done!\n");

                if(proc.getClass().getName().equals("java.lang.UNIXProcess")) {
                    /* get the PID on unix/linux systems */
                    try {
                        Field f = proc.getClass().getDeclaredField("pid");
                        f.setAccessible(true);
                        pid = f.getInt(proc);
                    } catch (Throwable e) {}
                }
            } else {
                System.out.println("Error: impossible to starting server!");
            }
        } catch (IOException e) {
            System.out.println("Impossible to compile the socket\n");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return pid;
    }
}
