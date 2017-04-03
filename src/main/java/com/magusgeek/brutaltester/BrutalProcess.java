package com.magusgeek.brutaltester;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

public class BrutalProcess {

    private Process process;
    private PrintStream out;
    private Scanner in;
    private InputStream error;
    
    public BrutalProcess(Process process) {
        this.process = process;
        out = new PrintStream(process.getOutputStream());
        in = new Scanner(process.getInputStream());
        error = process.getErrorStream();
    }
    
    public void clearErrorStream() throws IOException {
        while (error.available() != 0) {
            error.read();
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

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public Scanner getIn() {
        return in;
    }

    public void setIn(Scanner in) {
        this.in = in;
    }

    public InputStream getError() {
        return error;
    }

    public void setError(InputStream error) {
        this.error = error;
    }
}
