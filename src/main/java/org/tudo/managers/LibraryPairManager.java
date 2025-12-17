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
import org.tudo.LibraryPair;
import org.tudo.persistenceManagers.LibraryPairsPersistenceManager;
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
public class LibraryPairManager {
    private Project<URL> project;
    private File leafFile;
    private File dependentFile;
    private CallGraph callGraph;
    private DeclaredMethods prjDecMethods;

    private List<ObjectType> objTypesInLeaf;
    private List<ObjectType> objTypesInDependence;
    private List<DeclaredMethod> methodsInLeaf;
    private List<DeclaredMethod> methodsInDependent;
    private List<DeclaredMethod> unusedMethodsInLeafByDependence;
    private boolean isLeafBloatedInDependence;

    private MethodsManager methodsManager;
    private LibraryPairsPersistenceManager libraryPairsPersistenceManager;
    private LibraryPair libraryPair;

    public LibraryPairManager () {
        this.methodsManager = new MethodsManager();
        this.libraryPairsPersistenceManager = new  LibraryPairsPersistenceManager();
    }

    public void resetProject(){
        this.project = null;
        this.leafFile = null;
        this.dependentFile = null;
        this.callGraph = null;
        this.prjDecMethods = null;

        this.objTypesInLeaf = null;
        this.objTypesInDependence = null;
        this.methodsInLeaf = null;
        this.methodsInDependent = null;
        this.unusedMethodsInLeafByDependence = null;
        this.isLeafBloatedInDependence = false;

        this.libraryPair = null;

        System.gc();
    }

    /**
     * inits project for OPAL static analysis of each leaf- & dependent-library.
     */
    public void initProject(File leafFile, File dependentFile, Config config, String leafCoordinates, String dependentCoordinates) {
        this.leafFile = leafFile;
        this.dependentFile = dependentFile;
        this.project = Project$.MODULE$.apply(
                new File[]{this.dependentFile, this.leafFile},
                new File[]{},
                GlobalLogContext$.MODULE$,
                config
        );
        this.callGraph = this.project.get(FTACallGraphKey$.MODULE$);
        this.prjDecMethods = this.project.get(DeclaredMethodsKey$.MODULE$);

        System.out.println("leaf fileName: " + this.leafFile.getName());
        System.out.println("dependentFile: " + this.dependentFile.getName());

        this.objTypesInLeaf = this.sortObjTypesByFile(project.classFilesWithSources(), this.leafFile);
        this.objTypesInDependence = this.sortObjTypesByFile(project.classFilesWithSources(), this.dependentFile);

        this.methodsInLeaf = methodsManager.sortMethodsByFile(prjDecMethods, objTypesInLeaf, project);
        System.out.println("-- Total leaf methods with body : " + methodsInLeaf.size());
        this.methodsInDependent = methodsManager.sortMethodsByFile(prjDecMethods, objTypesInDependence, project);
        System.out.println("-- Total dependent methods with body : " + methodsInDependent.size());

        this.unusedMethodsInLeafByDependence = methodsManager.getUnusedMethodsInFileByOtherFile(methodsInLeaf, objTypesInDependence, callGraph);
        System.out.println("-- Total unused in leaf by dependent library: " + unusedMethodsInLeafByDependence.size());
        if(methodsInLeaf.size() == unusedMethodsInLeafByDependence.size()) {
            isLeafBloatedInDependence = true;
            System.out.println("--> " + leafFile.getName() + " is bloated in " + dependentFile.getName());
        }

        //Store analysis result into DB
        this.libraryPair = new LibraryPair(leafCoordinates, dependentCoordinates, methodsInLeaf.size(), methodsInDependent.size(), callGraph.numEdges(), unusedMethodsInLeafByDependence.size(), isLeafBloatedInDependence);
        libraryPairsPersistenceManager.save(libraryPair);
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
        ClassFile classFile;
        URL src;
        String srcStr;
        for (Tuple2<ClassFile, URL> t : CollectionConverters.asJava(projectClassFilesWSources)) {
            classFile = t._1();
            src = t._2();
            srcStr = src.toString();
//            System.out.println("source: " + srcStr);
//            System.out.println("jarFile name: " + jarFile.getName() + ", type: " + classFile.thisType().toString());
            if (srcStr.contains(file.getName())) typesInFile.add(classFile.thisType());
        }
        return typesInFile;
    }

}
