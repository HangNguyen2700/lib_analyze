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
    private final Project<URL> project;

    public MethodsManager(Project<URL> project) {
        this.project = project;
    }

    public boolean checkIfDecMethodInJar(DeclaredMethod decMethod, List<ObjectType> typesInJar) {
//        Iterable<Tuple2<ClassFile, URL>> projectClassFilesWSources = project.classFilesWithSources();
//        List<ObjectType> typesInJar = this.sortClassFileToJar(projectClassFilesWSources, jarFile);
        return typesInJar.contains(decMethod.declaringClassType());
    }

    // collect all methods with body in jar
    public List<DeclaredMethod> collectAllDecMethodsInJar(List<ObjectType> typesInJar, DeclaredMethods prjDecMethods) {
        List<DeclaredMethod> decMethodInJar = new ArrayList<>();
        Iterator<ClassFile> classFileIt = project.allProjectClassFiles().iterator();
        // loop through all classFiles in project, using its type to check if classFile belongs Jar
        while (classFileIt.hasNext()) {
            ClassFile classFile = classFileIt.next();
            if (!typesInJar.contains(classFile.thisType())) continue;
            // if classFile belongs Jar --> loop through all Methods in classFile, look for it in form of
            // DeclaredMethod(using prjDeclaredMethods.apply(m), useful for the analysis) and then add to the list
            Iterator<Method> methodIt = classFile.methods().iterator();
            while (methodIt.hasNext()) {
                Method m = methodIt.next();
                if (m.body().isDefined()) {
                    decMethodInJar.add(prjDecMethods.apply(m)); // yields a DefinedMethod (a DeclaredMethod)
                }
            }
        }
        return decMethodInJar;
    }

    public List<DeclaredMethod> getUnusedMethodsInJar(List<DeclaredMethod> decMethodsInJar, CallGraph callGraph) {
        List<DeclaredMethod> unused = new ArrayList<>();
        for(DeclaredMethod decMethod : decMethodsInJar) {
            Iterator<Tuple3<DeclaredMethod, Object, Object>> callers = callGraph.callersOf(decMethod).iterator();
            if(callers.isEmpty()) unused.add(decMethod);
        }
        return unused;
    }

    public List<DeclaredMethod> getUnusedMethodsInJarByJar(List<DeclaredMethod> decMethodsInJar, CallGraph callGraph, List<ObjectType> typesInCallerJar) {
        List<DeclaredMethod> unused = new ArrayList<>();
        boolean calledByJar = false;

        for(DeclaredMethod decMethod : decMethodsInJar) {
            Iterator<Tuple3<DeclaredMethod, Object, Object>> callers = callGraph.callersOf(decMethod).iterator();
            calledByJar = false;
            while (callers.hasNext()) {
                Tuple3<DeclaredMethod, Object, Object> t = callers.next();
                DeclaredMethod caller = t._1();
                calledByJar = this.checkIfDecMethodInJar(caller, typesInCallerJar);
                if(calledByJar) break;
            }
            if(!calledByJar) unused.add(decMethod);
        }

        return unused;
    }
}
