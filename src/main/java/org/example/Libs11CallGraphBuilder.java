package org.example;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.opalj.br.ClassFile;
import org.opalj.br.DeclaredMethod;
import org.opalj.br.Method;
import org.opalj.br.ObjectType;
import org.opalj.br.analyses.DeclaredMethods;
import org.opalj.br.analyses.DeclaredMethodsKey$;
import org.opalj.br.analyses.Project;
import org.opalj.br.analyses.Project$;
import org.opalj.log.GlobalLogContext$;
import org.opalj.tac.cg.CallGraph;
import org.opalj.tac.cg.FTACallGraphKey$;
import scala.Tuple2;
import scala.Tuple3;
import scala.collection.Iterable;
import scala.collection.Iterator;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Libs11CallGraphBuilder {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: LibsCallGraphBuilder <A.jar> <B.jar>");
            System.exit(1);
        }
        File aFile = new File(args[0]);
        File bFile = new File(args[1]);

        // 1) Build a sane config for "treat as libraries"
        String cfg = ""
                + "org.opalj.br.analyses.cg { \n"
                + "  ClosedPackagesKey { analysis = org.opalj.br.analyses.cg.AllPackagesClosed }\n"
                + "  InitialEntryPointsKey { analysis = org.opalj.br.analyses.cg.LibraryEntryPointsFinder }\n"
                + "}\n";

        Config overrideConfig = ConfigFactory.parseString(cfg).withFallback(ConfigFactory.load());

// 2) Create the Project: A+B are ANALYZED (project files); JRE is the library CP
//        File jreHome = new File(System.getProperty("java.home"));
        Project<URL> project = Project$.MODULE$.apply(
                new File[]{ aFile, bFile },
                new File[]{},
                GlobalLogContext$.MODULE$,
                overrideConfig
        );

        //
        // 3) Compute the call graph using FTA (Function-Type Analysis)
        //
        CallGraph callGraph = project.get(FTACallGraphKey$.MODULE$); // uses FTACallGraphKey :contentReference[oaicite:1]{index=1}

       // Map each loaded class to the jar it came from, so we can tell "belongs to A" vs "belongs to B"
        List<ObjectType> typesInA = new ArrayList<>();
        List<ObjectType> typesInB = new ArrayList<>();
        Iterable<Tuple2<ClassFile, URL>> classFilesIt = project.classFilesWithSources();
        for (Tuple2<ClassFile, URL> t : CollectionConverters.asJava(classFilesIt)) {
            ClassFile classFile = t._1();
            URL src = t._2();
            String srcStr = src.toString();
//            System.out.println("source: " + srcStr);
//            System.out.println("aFile name: " + aFile.getName() + ", type: " + classFile.thisType().toString());
//            System.out.println("bFile name: " + bFile.getName() + ", type: " + classFile.thisType().toString());
            if (srcStr.contains(aFile.getName())) typesInA.add(classFile.thisType());
            if (srcStr.contains(bFile.getName())) typesInB.add(classFile.thisType());
        }

        // Helper to check if a DeclaredMethod belongs to A/B using its declared type
        Predicate<DeclaredMethod> isFromA =
                dm -> typesInA.contains(dm.declaringClassType());
        Predicate<DeclaredMethod> isFromB =
                dm -> typesInB.contains(dm.declaringClassType());

        // Get DeclaredMethods so we can talk to the call graph
        DeclaredMethods declaredMethods = project.get(DeclaredMethodsKey$.MODULE$);

        // Collect all declared methods *defined by* classes in A (limit to those with bodies)
        List<DeclaredMethod> methodsInA = new ArrayList<>();
        Iterator<ClassFile> classFileIt = project.allProjectClassFiles().iterator();
        while (classFileIt.hasNext()) {
            ClassFile classFile = classFileIt.next();
            if (!typesInA.contains(classFile.thisType())) continue;
            Iterator<Method> methodIt = classFile.methods().iterator();
            while (methodIt.hasNext()) {
                Method m = methodIt.next();
                if (m.body().isDefined()) {
                    methodsInA.add(declaredMethods.apply(m)); // yields a DefinedMethod (a DeclaredMethod)
                }
            }
        }

        // Compute:
        //   1) methods in A with NO callers from A or B (unused anywhere within A+B)
        //   2) methods in A with NO callers from B (unused by B)
        List<DeclaredMethod> unusedAnywhere = new ArrayList<>();
        List<DeclaredMethod> unusedByB = new ArrayList<>();
        List<DeclaredMethod> unused = new ArrayList<>();
        System.out.println("count m: " + methodsInA.size() );
        for (DeclaredMethod m : methodsInA) {
            boolean calledByAB = false;
            boolean calledByBOnly = false;

            // Iterate callers: callersOf returns (callerDeclMethod, pc, isDirect)
            // loop all caller of DeclaredMethod m, check if this caller belongs A,B or not
            Iterator<Tuple3<DeclaredMethod, Object, Object>> callers = callGraph.callersOf(m).iterator();
            if(callers.isEmpty()) unused.add(m);
            while (callers.hasNext()) {
                Tuple3<DeclaredMethod, Object, Object> t = callers.next();
                DeclaredMethod caller = t._1();

                //true if caller is from A or B
                if (isFromA.or(isFromB).test(caller)) {
                    calledByAB = true;
                }
                if (isFromB.test(caller)) {
                    calledByBOnly = true;
                }

                if (calledByAB && calledByBOnly) break;
            }



            if (!calledByAB) unusedAnywhere.add(m);
            if (!calledByBOnly) unusedByB.add(m);
        }



        // Pretty-print results
        System.out.println("#####Methods in A not called anywhere === count: " + unused.size());

        System.out.println("#####Methods in A not called anywhere within {A,B} === count: " + unusedAnywhere.size());
//        for (DeclaredMethod dm : unusedAnywhere) {
//            System.out.println(dm.toJava());
//        }

        System.out.println("##### Methods in A not called by B === count: " + unusedByB.size());
//        for (DeclaredMethod dm : unusedByB) {
//            System.out.println(dm.toJava());
//        }

    }
}
