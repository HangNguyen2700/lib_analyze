package org.example;

import com.typesafe.config.Config;
import org.opalj.br.ClassFile;
import org.opalj.br.Method;
import org.opalj.br.DeclaredMethod;
import org.opalj.br.analyses.DeclaredMethods;
import org.opalj.br.analyses.DeclaredMethodsKey$;
import org.opalj.br.analyses.Project;
import org.opalj.br.analyses.Project$;
import org.opalj.br.fpcf.properties.Context;
import org.opalj.log.GlobalLogContext$;
import org.opalj.tac.cg.CallGraph;
import org.opalj.tac.cg.FTACallGraphKey$;

import org.tudo.managers.MethodsManager;
import scala.Option;
import scala.Tuple2;

import java.io.File;
import java.net.URL;
import java.util.*;

public class LibraryPairManager2 {
    private final Project<URL> project;
    private final File leafFile;
    private final File dependentFile;
    private MethodsManager methodsManager;

    public LibraryPairManager2(File leafFile, File dependentFile, Config config) {
        this.leafFile = leafFile;
        this.dependentFile = dependentFile;

        // Option B: both JARs as project JARs → full method bodies
        this.project = Project$.MODULE$.apply(
                new File[]{leafFile, dependentFile},
                new File[]{},
                GlobalLogContext$.MODULE$,
                config
        );
        this.methodsManager = new MethodsManager(project);
    }

    /**
     * Static analysis using OPAL for each leaf- & dependent-library.
     * Counts how many methods in the leaf library are NOT reachable
     * from any method in the dependent library (according to the call graph).
     */
    public void analyzeLibraryPair() {
        System.out.println("Leaf library      : " + leafFile.getName());
        System.out.println("Dependent library : " + dependentFile.getName());

        // 1) Get call graph and declared methods
        CallGraph callGraph = project.get(FTACallGraphKey$.MODULE$);
        DeclaredMethods prjDecMethods = project.get(DeclaredMethodsKey$.MODULE$);

        // 2) Collect methods with body for each JAR
        List<DeclaredMethod> leafMethods = new ArrayList<>();
        List<DeclaredMethod> dependentMethods = new ArrayList<>();

        String leafJarName = leafFile.getName();
        String depJarName  = dependentFile.getName();

        scala.collection.Iterator<ClassFile> cfIt = project.allProjectClassFiles().iterator();
        while (cfIt.hasNext()) {
            ClassFile cf = cfIt.next();

            // Where did this class file come from? (the JAR URL)
            Option<URL> srcOpt = project.source(cf);
            if (srcOpt.isEmpty()) continue;

            String src = srcOpt.get().toString();
            boolean fromLeafJar = src.contains(leafJarName);
            boolean fromDepJar  = src.contains(depJarName);

            if (!fromLeafJar && !fromDepJar) {
                // Class is from some other source (e.g. JDK classes if present)
                continue;
            }

            // Iterate over methods and keep only those with a body
            scala.collection.Iterator<Method> mIt = cf.methods().iterator();
            while (mIt.hasNext()) {
                Method m = mIt.next();
                if (!m.body().isDefined()) continue;

                DeclaredMethod dm = prjDecMethods.apply(m);
                if (fromLeafJar) {
                    leafMethods.add(dm);
                } else if (fromDepJar) {
                    dependentMethods.add(dm);
                }
            }
        }

        int totalLeafWithBody = leafMethods.size();
        System.out.println("Total leaf methods with body : " + totalLeafWithBody);
        System.out.println("Total dependent methods with body: " +  dependentMethods.size());

        // Early exit if nothing to analyze
        if (totalLeafWithBody == 0) {
            System.out.println("Reachable from dependent      : 0");
            System.out.println("UNUSED by dependent           : 0");
            return;
        }

        // 3) Compute methods reachable from the dependent library via the call graph
        Set<DeclaredMethod> reachableFromDependent = new HashSet<>();
        Deque<DeclaredMethod> worklist = new ArrayDeque<>();

        // Seed = all methods in the dependent JAR
        for (DeclaredMethod dm : dependentMethods) {
            if (reachableFromDependent.add(dm)) {
                worklist.add(dm);
            }
        }

        // BFS over the call graph: follow callees
        while (!worklist.isEmpty()) {
            DeclaredMethod current = worklist.removeFirst();

            scala.collection.Iterator<
                    Tuple2<Object, scala.collection.Iterator<Context>>
                    > calleesIt = callGraph.calleesOf(current);

            while (calleesIt.hasNext()) {
                Tuple2<Object, scala.collection.Iterator<Context>> entry = calleesIt.next();
                scala.collection.Iterator<Context> ctxIt = entry._2();

                while (ctxIt.hasNext()) {
                    Context ctx = ctxIt.next();
                    DeclaredMethod callee = ctx.method();

                    if (reachableFromDependent.add(callee)) {
                        worklist.add(callee);
                    }
                }
            }
        }

        // 4) Count leaf methods that are reachable / unreachable from dependent
        long usedLeafMethods = leafMethods.stream()
                .filter(reachableFromDependent::contains)
                .count();

        long unusedLeafMethods = totalLeafWithBody - usedLeafMethods;

        System.out.println("Reachable from dependent      : " + usedLeafMethods);
        System.out.println("UNUSED by dependent           : " + unusedLeafMethods);
    }
}
