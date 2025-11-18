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
                System.out.println("Error rolling back transaction");
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
                System.out.println("Error saving library ");
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

    public List<Library> getLibrariesByDependency(String coordinates) {
        if (coordinates == null || coordinates.isBlank()) {
            System.out.println("getLibrariesByDependency: coordinates of dependency is null or blank");
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
                System.out.println("Error finding libraries by dependency coordinates: " + coordinates);
                e.printStackTrace();
                safeRollback(transaction);
            }
            return Collections.emptyList();
        }
    }
}
