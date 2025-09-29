package org.example;

import java.io.File;
import java.net.URL;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import org.opalj.br.DeclaredMethod;
import org.opalj.br.analyses.DeclaredMethods;
import org.opalj.br.analyses.DeclaredMethodsKey$;
import org.opalj.br.analyses.Project;
import org.opalj.br.analyses.Project$;
import org.opalj.log.GlobalLogContext$;
import org.opalj.tac.cg.*;
import scala.Tuple3;
import scala.collection.IterableOnce;
import scala.collection.Iterator;

public class CallGraphBuilder {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java CallGraphBuilder <classes-dir-or-jar> <output-json>");
            System.exit(1);
        }
        String inputPath  = args[0];
        String outputJson = args[1];

        // 1. Load default config and override to library mode: It reads OPAL’s default configuration and then overrides
        // the analysis mode to  LibraryWithClosePackagesAssumption, which tells OPAL to treat your codebase like a library, not an application.
        Config baseConfig = ConfigFactory.load();
        Config libConfig  = baseConfig.withValue(
                "org.opalj.analysis.Mode",
                ConfigValueFactory.fromAnyRef("LibraryWithClosePackagesAssumption")
        );
        // OPAL offers DesktopApplication, LibraryWithClosePackagesAssumption,
        // and LibraryWithOpenPackagesAssumption for different library assumptions :contentReference[oaicite:0]{index=0}.

        // 2. Create an OPAL Project in library mode: By pointing at a directory or JAR of compiled classes
        // (<classes-dir-or-jar>), it builds an internal representation of all those class files.
        Project<URL> project =  Project$.MODULE$.apply(
                new File(inputPath),
                GlobalLogContext$.MODULE$,
                libConfig
        );

        // 3. Compute the call graph using CHA: It asks OPAL for the CHACallGraphKey
        CallGraph callGraph = project
                .get(FTACallGraphKey$.MODULE$);

        // 4. Collect all declared methods: It retrieves a DeclaredMethods object, essentially the full list of methods
        // that OPAL saw in your codebase.
        DeclaredMethods declaredMethods = project.get(DeclaredMethodsKey$.MODULE$);

        // 5. Print all edges
        // Iterate through each method and its callers
        System.out.println("CALL GRAPH EDGES (caller → callee):");
        Iterator<DeclaredMethod> dmIt = declaredMethods.declaredMethods();
        int i = 0;
        int j = 0;
        while (dmIt.hasNext()) {
            DeclaredMethod callee = dmIt.next();
            IterableOnce<Tuple3<DeclaredMethod, Object, Object>> callers =
                    callGraph.callersOf(callee);  // def callersOf(m): IterableOnce[(DeclaredMethod, Int, Boolean)] :contentReference[oaicite:0]{index=0}
            Iterator<Tuple3<DeclaredMethod, Object, Object>> cIt = callers.iterator();

            if(!cIt.hasNext()) {
                String calleeClass = callee.declaringClassType().toJava();
                System.out.println("i: " + i + "this method has no callers: " + calleeClass + "#" + callee.name());
            }
            while (cIt.hasNext()) {
                Tuple3<DeclaredMethod, Object, Object> t = cIt.next();
                DeclaredMethod caller   = t._1();
                int            pc       = (Integer) t._2();
                boolean        isDirect = (Boolean) t._3();
                String callerClass = caller.declaringClassType().toJava();
                String calleeClass = callee.declaringClassType().toJava();
                System.out.println("i: " + i);
                System.out.printf(
                        "-- %s#%s → %s#%s  [pc=%d, direct=%b]%n",
                        callerClass, caller.name(),
                        calleeClass, callee.name(),
                        pc, isDirect
                );
                if(!cIt.hasNext()) {
                    System.out.println("-- leaf class, finish 1 chain");
                }
            }


            System.out.println(dmIt.hasNext());
            if(!dmIt.hasNext()){
                String calleeClass = callee.declaringClassType().toJava();
                System.out.println("i: " + i + ", method: " + calleeClass + "#" + callee.name());
            }
            i++;
        }

        // 6. SERIALIZE to JSON using writeCG, passing in our DeclaredMethods :contentReference[oaicite:0]{index=0}
        File outFile = new File(args[1]);
        CallGraphSerializer$.MODULE$.writeCG(callGraph, outFile, declaredMethods);
        System.out.println("Wrote call graph to " + outFile);
    }
}
