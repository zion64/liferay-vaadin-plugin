package com.arcusys.liferay.vaadinplugin.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WidgetsetCompiler {

    private String widgetset;
    private String outputDir;
    private List<File> classpathEntries;

    private Process process;
    private boolean controlledTermination;
    private ILog outptLog;

    public WidgetsetCompiler(ILog outptLog)
    {
        this.outptLog = outptLog;
    }

    public void compileWidgetset() throws IOException, InterruptedException {
        String classpathSeparator = System.getProperty("path.separator");

        boolean someNotExists = false;
        StringBuilder nonExistedFiles = new StringBuilder("ERROR: Can't found files: ");
        for (File classpathEntry : classpathEntries) {
            if(!classpathEntry.exists()){
                someNotExists = true;
                nonExistedFiles.append(classpathEntry.getName() + " (" + classpathEntry.getAbsolutePath() + ")<br/>");
            }
        }

        if( someNotExists) {
            System.out.println(nonExistedFiles.toString());
            outptLog.log(nonExistedFiles.toString());
            terminate();
        }

        StringBuilder classpath = new StringBuilder();
        for (File classpathEntry : classpathEntries) {
            classpath.append(classpathEntry).append(classpathSeparator);
        }

        ArrayList<String> args = new ArrayList<String>();
        args.add(getJava());

        args.add("-Djava.awt.headless=true");
        args.add("-Dgwt.nowarn.legacy.tools");
        args.add("-Xss8M");
        args.add("-Xmx512M");
        args.add("-XX:MaxPermSize=512M");

        if (System.getProperty("os.name").equals("mac")) {
            args.add("-XstartOnFirstThread");
        }

        args.add("-classpath");
        args.add(classpath.toString().replaceAll(" ", "\\ "));



        String compilerClass = "com.google.gwt.dev.Compiler";
        args.add(compilerClass);

//        args.add("-out");
        args.add("-war");
        args.add(outputDir);
//        CompilerOptions compilerOpetions = new CompilerOptionsImpl();
//        //compilerOpetions.
//        Compiler compiler = new Compiler(compilerOpetions);

        /*-
        String style = prefStore
                .getString(VaadinPlugin.PREFERENCES_WIDGETSET_STYLE);
        if ("DRAFT".equals(style)) {
            args.add("-style");
            args.add("PRETTY");
            args.add("-draftCompile");
        } else if (!"".equals(style)) {
            args.add("-style");
            args.add(style);
        }*/

        // String parallelism = prefStore
        // .getString(VaadinPlugin.PREFERENCES_WIDGETSET_PARALLELISM);
        // if ("".equals(parallelism)) {
        args.add("-localWorkers");
        args.add("" + Runtime.getRuntime().availableProcessors());
        // } else {
        // args.add("-localWorkers");
        // args.add(parallelism);
        // }

        // if (verbose) {
        args.add("-logLevel");

        args.add("INFO");
       // args.add("INFO");
        // } else {
        // args.add("-logLevel");
        // args.add("WARN");
        // }

        args.add(widgetset);

        final String[] argsStr = new String[args.size()];
        args.toArray(argsStr);
        for (String arg : argsStr) {
            System.out.print(arg + " ");
        }
        System.out.println("");

        controlledTermination = false;

        process = new ProcessBuilder(argsStr).start();

        if (outptLog != null) {
            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        BufferedReader stdInput = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        String s = null;
                        while ((s = stdInput.readLine()) != null) {
                            //getUI().getSession().getLockInstance().lock();
                            System.out.println(s);
                            outptLog.log(s);
                        }
                    } catch (IOException e) {
                    }
                }
            });

            executor.execute(new Runnable() {
                public void run() {
                    try {
                        BufferedReader stdError = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()));
                        String s = null;
                        while ((s = stdError.readLine()) != null) {
                            System.out.println(s);
                            outptLog.log(s);
                        }
                    } catch (IOException e) {
                    }
                }
            });
        }

        process.waitFor();

        if (process.exitValue() != 0 && outptLog != null
                && !controlledTermination) {
            outptLog.log("ERROR: Compilation ended due to an error.");
        }
    }

    public void terminate() {
        controlledTermination = true;
        process.destroy();
    }

    public void setWidgetset(String widgetset) {
        this.widgetset = widgetset;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public void setClasspathEntries(List<File> classpathEntries) {
        this.classpathEntries = new ArrayList<File>(classpathEntries);
    }

    /**
     * Returns the proper Java command.
     *
     * First checks $JAVA_HOME/bin/java, then returns 'java', which is expected
     * to be found from PATH.
     *
     * @return name of java executable.
     */
    private String getJava() {
        String javaHome = System.getenv("JAVA_HOME");
        String path = javaHome + "/bin/java";

        // Can't use isExecutable() for this check as it's Java 6.
        File file = new File(path);
        if (file.exists()) {
            return path;
        }

        return "java";
    }

//    public void setCompileOutputConsumer(
//            CompileOutputConsumer compileOutputConsumer) {
//        this.compileOutputConsumer = compileOutputConsumer;
//    }

//    public interface CompileOutputConsumer {
//        public void consumeLine(String line);
//    }
}
