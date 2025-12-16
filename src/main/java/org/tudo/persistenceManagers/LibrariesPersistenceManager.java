package org.tudo.persistenceManagers;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.tudo.LeafLibrary;
import org.tudo.Library;
import org.tudo.utils.HibernateUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LibrariesPersistenceManager {
    private static void safeRollback(Transaction transaction) {
        if (transaction != null && transaction.isActive()) {
            try {
                transaction.rollback();
                System.out.println("Transaction rollback successfully");
            } catch (Exception ignored) {
                System.err.println("Error rolling back transaction");
                ignored.printStackTrace();
            }
        }
    }

    public void save(Library library) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.merge(library);
                transaction.commit();
            } catch (ConstraintViolationException dup) {
                System.out.println("Duplicated library found");
                safeRollback(transaction);
            } catch (Exception e) {
                System.err.println("Error saving library ");
                safeRollback(transaction);
                e.printStackTrace();
            }
        }
    }

//    public LeafLibrary getLeafByCoordinates(String coordinates) {
//        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
//            Transaction transaction = session.beginTransaction();
//            try {
//                LeLibrary result = session
//                        .createQuery(
//                                "select ll from LeafLibrary ll order by function('random')",
//                                LeafLibrary.class)
//                        .setReadOnly(true); // hint: read-only for performance
//
//                transaction.commit();
//                return new LeafLibrary(result.getGroupId(), result.getArtifactId(), result.getVersion(), result.getMavenCentralJarUri());
//            } catch (Exception e) {
//                System.out.println("Error getting random leaf libraries ");
//                e.printStackTrace();
//                safeRollback(transaction);
//            }
//            return null;
//        }
//    }

    public Library getLibraryByCoordinates(String coordinates) {
        if (coordinates == null || coordinates.isBlank()) {
            System.out.println("getLibraryByCoordinates: coordinates of dependency is null or blank");
            return null;
        }
        String[] splitted = coordinates.split(":");
        if (splitted.length != 3) {
            System.err.println("getLibraryByCoordinates: Invalid coordinates: " + coordinates +
                    " (expected format: groupId:artifactId:version)");
            return null;
        }

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Library library = session.createQuery(
                                "select l from Library l " +
                                        "where l.groupId = :groupId " +
                                        "and l.artifactId = :artifactId " +
                                        "and l.version = :version " +
                                        "and l.isLeaf = true",
                                Library.class)
                        .setParameter("groupId", splitted[0])
                        .setParameter("artifactId", splitted[1])
                        .setParameter("version", splitted[2])
                        .setReadOnly(true)
                        .uniqueResult();
                transaction.commit();
                return library;
            } catch (Exception e) {
                System.err.println("getLibraryByCoordinates: Error finding library by coordinates: " + coordinates);
                e.printStackTrace();
                safeRollback(transaction);
            }
            return null;
        }
    }

    /**
     * gets alls libraries that depends on the given leaf library by leaf coordinates
     * @param coordinates
     */
    public List<Library> getLibrariesByLeafCoordinates(String coordinates) {
        if (coordinates == null || coordinates.isBlank()) {
            System.out.println("getLibrariesByLeafCoordinates: coordinates of dependency is null or blank");
            return Collections.emptyList();
        }

        String coordinatesInput = "%" + coordinates + "%";

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<Library> result = session
                        .createQuery(
                                "select l from Library l " +
                                        "where l.dependencyCoordinates like :coordinatesInput",
                                Library.class)
                        .setParameter("coordinatesInput", coordinatesInput)
                        .setReadOnly(true) // hint: read-only for performance
                        .list();
                transaction.commit();
                return result;
            } catch (Exception e) {
                System.err.println("getLibrariesByLeafCoordinates: Error finding libraries by leaf coordinates: " + coordinates);
                e.printStackTrace();
                safeRollback(transaction);
            }
            return Collections.emptyList();
        }
    }

    public Set<Library> getRandom(int n) {
        if (n <= 0) {
            System.out.println("getRandom: Number of libraries to be fetched must be a positive integer");
            return Collections.emptySet();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<Library> result = session
                        .createQuery(
                                "select l from Library l order by function('random')",
                                Library.class)
                        .setMaxResults(n)
                        .setReadOnly(true) // hint: read-only for performance
                        .list();
                transaction.commit();
                return new HashSet<>(result);
            } catch (Exception e) {
                System.err.println("getRandom: Error getting random libraries ");
                e.printStackTrace();
                safeRollback(transaction);
            }
            return Collections.emptySet();
        }
    }
}
