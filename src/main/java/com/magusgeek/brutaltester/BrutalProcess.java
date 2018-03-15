package com.magusgeek.brutaltester;

import java.io.*;
import java.util.Scanner;

public class BrutalProcess {

    private Process process;
    private PrintStream out;
    private Scanner in;
    private BufferedReader error;
    
    public BrutalProcess(Process process) {
        this.process = process;
        out = new PrintStream(process.getOutputStream());
        in = new Scanner(process.getInputStream());
        error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    }
    
    public void clearErrorStream(OldGameThread thread, String prefix) throws IOException {
        while (error.ready()) {
            thread.log(prefix + error.readLine());
        }
    }
    
    public void destroy() throws IOException {
        out.close();
        in.close();
        error.close();
        process.destroy();
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public PrintStream getOut() {
        return out;
    }

    public Scanner getIn() {
        return in;
    }

    public BufferedReader getError() {
        return error;
    }
}
