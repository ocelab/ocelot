package it.unisa.ocelot.serverSocket;

import sun.tools.jar.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.locks.ReentrantLock;

public class ServerSocketThread extends Thread {
    private ServerSocket serverSocket;
    private boolean isRunning;
    private int pid;
    private final ReentrantLock lock;

    public ServerSocketThread () {
        this.serverSocket = new ServerSocket();
        this.isRunning = true;

        this.lock = new ReentrantLock();
        this.pid = -1;

        initialize();
    }


    @Override
    public void run() {
        while (isRunning) {
            //Check if server process is still alive
            boolean isServerAlive = isStillAlive(pid);

            if (!isServerAlive) {
                //Reboot
                pid = serverSocket.startServer();
                System.out.println("New Server PID Process: " + pid);
            } else {
                System.out.println("Server PID Process: " + pid);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void initialize () {
        serverSocket.compile();
        pid = serverSocket.startServer();
        System.out.println("Server PID Process: " + pid);
    }

    @Override
    public void interrupt() {
        lock.lock();

        try {
            this.isRunning = false;
        } finally {
            lock.unlock();
        }
    }

    private boolean isStillAlive (int pid) {
        String pidStr = String.valueOf(pid);
        String os = System.getProperty("os.name").toLowerCase();
        String command = null;

        if (os.contains("Win")) {
            command = "cmd /c tasklist /FI \"PID eq " + pidStr + "\"";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            command = "ps -p " + pidStr;
        } else {
            return false;
        }

        return isProcessIdRunning(pidStr, command); // call generic implementation
    }

    private boolean isProcessIdRunning(String pid, String command) {
        try {
            Process pr = Runtime.getRuntime().exec(command);

            BufferedReader bReader = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String strLine = null;
            while ((strLine= bReader.readLine()) != null) {
                System.out.println(strLine);
                if (strLine.contains(" " + pid + " ")) {
                    return true;
                }
            }

            return false;
        } catch (Exception ex) {
            return true;
        }
    }


}
