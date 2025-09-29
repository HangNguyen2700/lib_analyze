package org.tudo.utils;

import org.tudo.sse.model.pom.Dependency;

import java.util.Locale;

public class PomUtils {
    public static boolean isActualDependency(Dependency dependency) {
        // optional? -> ignore
        if (dependency.isOptional()) return false;

        // test/provided/system scopes -> ignore
        String scope = trimString(dependency.getScope());
        if ("test".equals(scope) || "provided".equals(scope) || "system".equals(scope)) return false;

        // import scope is only supported on a dependency of type bom -> ignore
        if ("import".equals(scope)) return false;

        // Otherwise we count it as a real dependency
        return true;
    }

    public static String trimString(String string) {
//        return string == null ? "" : string.trim().toLowerCase(Locale.ROOT);
        return string == null ? "" : string.trim();
    }
}
