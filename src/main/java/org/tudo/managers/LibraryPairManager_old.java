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

/**
 * Loads 2 libraries into 1 project for OPAL static analysis.
 */
@Deprecated
public class LibraryPairManager_old {
    private final Project<URL> project;
    private final File leafFile;
    private final File dependentFile;
    private MethodsManager_old methodsManager;

//    public LibsManager(Project<URL> project, File aFile, File bFile) {
//        this.project = project;
//        this.aFile = aFile;
//        this.bFile = bFile;
//    }

    public LibraryPairManager_old(File leafFile, File dependentFile, Config config) {
        this.leafFile = leafFile;
        this.dependentFile = dependentFile;
        this.project = Project$.MODULE$.apply(
                new File[]{dependentFile, leafFile},
                new File[]{},
                GlobalLogContext$.MODULE$,
                config
        );
        this.methodsManager = new MethodsManager_old(project);
    }

    /**
     * Static analysis using OPAL for each leaf- & dependent-library.
     */
    public void analyzeLibraryPair() {
        CallGraph callGraph = project.get(FTACallGraphKey$.MODULE$);
        DeclaredMethods prjDecMethods = project.get(DeclaredMethodsKey$.MODULE$);

        System.out.println("leaf fileName: " + leafFile.getName());
        System.out.println("dependentFile: " + dependentFile.getName());

        List<ObjectType> objTypesInLeaf = this.sortObjTypesByFile(project.classFilesWithSources(), leafFile);
        List<ObjectType> objTypesInDependence = this.sortObjTypesByFile(project.classFilesWithSources(), dependentFile);

        List<DeclaredMethod> methodsInLeaf = methodsManager.sortMethodsByFile(prjDecMethods, objTypesInLeaf);
        System.out.println("-- Total leaf methods with body : " + methodsInLeaf.size());
        List<DeclaredMethod> methodsInDependent = methodsManager.sortMethodsByFile(prjDecMethods, objTypesInDependence);
        System.out.println("-- Total dependent methods with body : " + methodsInDependent.size());
//        System.out.println("###### List all methods in leaf: ");
//        for (DeclaredMethod method : methodsInLeaf) {
//            System.out.println(method.id() + method.name());
//        }
//        List<DeclaredMethod> unusedMethodsInLeaf = methodsManager.getUnusedMethodsInFile(methodsInLeaf, callGraph);
//        System.out.println("###### Count unused in A: " + unusedMethodsInLeaf.size());
        List<DeclaredMethod> unusedMethodsInLeafByDependence = methodsManager.getUnusedMethodsInFileByOtherFile(methodsInLeaf, objTypesInDependence, callGraph);
        System.out.println("-- Total unused in leaf by dependent library: " + unusedMethodsInLeafByDependence.size());
        if(methodsInLeaf.size() == unusedMethodsInLeafByDependence.size()) {
            System.out.println("--> " + leafFile.getName() + " is bloated in " + dependentFile.getName());
        }
    }

    /**
     * Sorts all ClassFiles in project by their source Files.
     *
     * @param projectClassFilesWSources
     *     An Iterable of tuples (ClassFile, URL) in the whole @Instance Project, where each
     *     ClassFile is paired with the URL from which it was loaded.
     * @param file
     *     The File whose name is used to filter the class files. In the typical
     *     use case this is a JAR file; any class whose source URL string contains
     *     file.getName() is considered to come from this file.
     * @return
     *     A List of ObjectType representing all ClassFiles whose bytecode
     *     originates from the given file/JAR.
     */
    public List<ObjectType> sortObjTypesByFile(Iterable<Tuple2<ClassFile, URL>> projectClassFilesWSources, File file) {
        List<ObjectType> typesInFile = new ArrayList<>();
        for (Tuple2<ClassFile, URL> t : CollectionConverters.asJava(projectClassFilesWSources)) {
            ClassFile classFile = t._1();
            URL src = t._2();
            String srcStr = src.toString();
//            System.out.println("source: " + srcStr);
//            System.out.println("jarFile name: " + jarFile.getName() + ", type: " + classFile.thisType().toString());
            if (srcStr.contains(file.getName())) typesInFile.add(classFile.thisType());
        }
        return typesInFile;
    }

}
