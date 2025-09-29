package org.tudo.managers;

import com.typesafe.config.Config;
import org.opalj.br.ClassFile;
import org.opalj.br.DeclaredMethod;
import org.opalj.br.ObjectType;
import org.opalj.br.analyses.DeclaredMethods;
import org.opalj.br.analyses.DeclaredMethodsKey$;
import org.opalj.br.analyses.Project;
import org.opalj.br.analyses.Project$;
import org.opalj.log.GlobalLogContext$;
import org.opalj.tac.cg.CallGraph;
import org.opalj.tac.cg.FTACallGraphKey$;
import scala.Tuple2;
import scala.collection.Iterable;
import scala.jdk.javaapi.CollectionConverters;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class LibsManager {
    private final Project<URL> project;
    private final File aFile;
    private final File bFile;
    private MethodsManager methodsManager;

//    public LibsManager(Project<URL> project, File aFile, File bFile) {
//        this.project = project;
//        this.aFile = aFile;
//        this.bFile = bFile;
//    }

    public LibsManager(File aFile, File bFile, Config config) {
        this.aFile = aFile;
        this.bFile = bFile;
        this.project = Project$.MODULE$.apply(
                new File[]{aFile, bFile},
                new File[]{},
                GlobalLogContext$.MODULE$,
                config
        );
        this.methodsManager = new MethodsManager(project);
    }

    public void analyze() {
        List<ObjectType> typesInA = this.sortClassFileToJar(project.classFilesWithSources(), aFile);
        List<ObjectType> typesInB = this.sortClassFileToJar(project.classFilesWithSources(), bFile);

        CallGraph callGraph = project.get(FTACallGraphKey$.MODULE$);
        DeclaredMethods prjDecMethods = project.get(DeclaredMethodsKey$.MODULE$);

        List<DeclaredMethod> decMethodsInA = methodsManager.collectAllDecMethodsInJar(typesInA, prjDecMethods);
        System.out.println("###### Count all methods in A: " + decMethodsInA.size());
        List<DeclaredMethod> unusedInA = methodsManager.getUnusedMethodsInJar(decMethodsInA, callGraph);
        System.out.println("###### Count unused in A: " + unusedInA.size());
        List<DeclaredMethod> unusedInAByB = methodsManager.getUnusedMethodsInJarByJar(decMethodsInA, callGraph, typesInB);
        System.out.println("###### Count unused in A by B: " + unusedInAByB.size());

    }

    public List<ObjectType> sortClassFileToJar(Iterable<Tuple2<ClassFile, URL>> projectClassFilesWSources, File jarFile) {
        List<ObjectType> typesInJar = new ArrayList<>();
        for (Tuple2<ClassFile, URL> t : CollectionConverters.asJava(projectClassFilesWSources)) {
            ClassFile classFile = t._1();
            URL src = t._2();
            String srcStr = src.toString();
//            System.out.println("source: " + srcStr);
//            System.out.println("jarFile name: " + jarFile.getName() + ", type: " + classFile.thisType().toString());
            if (srcStr.contains(jarFile.getName())) typesInJar.add(classFile.thisType());
        }
        return typesInJar;
    }

}
