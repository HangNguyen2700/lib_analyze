package org.tudo.managers;

import org.opalj.br.ClassFile;
import org.opalj.br.DeclaredMethod;
import org.opalj.br.Method;
import org.opalj.br.ObjectType;
import org.opalj.br.analyses.DeclaredMethods;
import org.opalj.br.analyses.Project;
import org.opalj.tac.cg.CallGraph;
import scala.Tuple3;
import scala.collection.Iterator;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MethodsManager {

    public MethodsManager() {
    }

    public boolean checkIfMethodInFile(DeclaredMethod method, List<ObjectType> objTypesInFile) {
//        Iterable<Tuple2<ClassFile, URL>> projectClassFilesWSources = project.classFilesWithSources();
//        List<ObjectType> objTypesInFile = this.sortClassFileToJar(projectClassFilesWSources, jarFile);
        return objTypesInFile.contains(method.declaringClassType());
    }

    /**
     * Sorts declared methods in the whole project into their belonging library files.
     *
     * @param prjMethods
     *  List of all declared methods in project.
     * @param objTypesInFile
     *  List of object types of a library file, used for identification if a declared method belongs to
     *  an object type (as well as belongs to the library file, which contains that object type) or not.
     */
    public List<DeclaredMethod> sortMethodsByFile(DeclaredMethods prjMethods, List<ObjectType> objTypesInFile, Project<URL> project) {
        List<DeclaredMethod> methodsInFile = new ArrayList<>();
        ClassFile classFile;
        Iterator<Method> methodIt;
        Method method;

        // loop through all classFiles in project, using its type to check if classFile belongs the given file
        Iterator<ClassFile> classFileIt = project.allProjectClassFiles().iterator();
        while (classFileIt.hasNext()) {
            classFile = classFileIt.next();
            if (!objTypesInFile.contains(classFile.thisType())) continue;

            // if classFile belongs the given file, it means all methods in that class file belongs to the
            // given file --> loop through all declaredMethods in classFile and then add to the list
            methodIt = classFile.methods().iterator();
            while (methodIt.hasNext()) {
                method = methodIt.next();
                // For every method that has a defined body m.body().isDefined(), the corresponding DeclaredMethod
                // is looked up in the project-wide DeclaredMethods index and added to the result.
                if (method.body().isDefined()) {
                    methodsInFile.add(prjMethods.apply(method));
                }
            }
        }
        return methodsInFile;
    }

    public List<DeclaredMethod> getUnusedMethodsInFile(List<DeclaredMethod> decMethodsInJar, CallGraph callGraph) {
        List<DeclaredMethod> unused = new ArrayList<>();
        Iterator<Tuple3<DeclaredMethod, Object, Object>> callers;

        for(DeclaredMethod decMethod : decMethodsInJar) {
            callers = callGraph.callersOf(decMethod).iterator();
            if(callers.isEmpty()) unused.add(decMethod);
        }
        return unused;
    }

    /**
     *
     * @param callees
     *  Declared Methods of the first library file to be checked if they are used or unused
     *  by other library file.
     * @param objTypesInCallerFile
     * @param callGraph
     * @return
     */
    public List<DeclaredMethod> getUnusedMethodsInFileByOtherFile(List<DeclaredMethod> callees, List<ObjectType> objTypesInCallerFile, CallGraph callGraph) {
        List<DeclaredMethod> unused = new ArrayList<>();
        boolean calledByOtherFile = false;
        Iterator<Tuple3<DeclaredMethod, Object, Object>> callers;
        Tuple3<DeclaredMethod, Object, Object> tuple;
        DeclaredMethod caller;

        System.out.println("callgraph's edges: " + callGraph.numEdges());

        for(DeclaredMethod callee : callees) {
            calledByOtherFile = false;
            callers = callGraph.callersOf(callee).iterator();
//            System.out.println("callee: " + callee.id() + callee.name());
            while (callers.hasNext()) {
                tuple = callers.next();
                caller = tuple._1();
                //Here: true when caller is located in DependentFile, false otherwise (--> in LeafFile)
                calledByOtherFile = this.checkIfMethodInFile(caller, objTypesInCallerFile);
//                System.out.println("--> " + caller.id() + caller.name() + " callee is called by caller: " + calledByOtherFile);
                if(calledByOtherFile) {
                    //declaringClassType is the class where contains callee (or the class extends it via inheritance)
//                    System.out.println("caller " + caller.id() + caller.name() + " uses method callee " + callee.declaringClassType() + " " + " " + callee.id() + callee.name());
                    break;
                }
            }
            if(!calledByOtherFile) unused.add(callee);
        }

        return unused;
    }
}
