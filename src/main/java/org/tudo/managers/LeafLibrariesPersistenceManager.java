package org.tudo.managers;

import jakarta.persistence.*;
//import jakarta.transaction.Transaction;
import org.hibernate.*;

import org.hibernate.exception.ConstraintViolationException;
import org.tudo.LeafLibrary;
import org.tudo.utils.HibernateUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LeafLibrariesPersistenceManager {

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

    public void save(LeafLibrary leafLibrary) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.merge(leafLibrary);
                transaction.commit();
            } catch (ConstraintViolationException dup) {
                System.out.println("Duplicated leaf library found");
                safeRollback(transaction);
            } catch (Exception e) {
                System.out.println("Error saving leaf library ");
                safeRollback(transaction);
//            e.printStackTrace();
            }
        }
    }

    public Set<LeafLibrary> getRandom(int n) {
        if (n <= 0) {
            System.out.println("Number of leaf libraries to be fetched must be a positive integer");
            return Collections.emptySet();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                List<LeafLibrary> result = session
                        .createQuery(
                                "select ll from LeafLibrary ll order by function('random')",
                                LeafLibrary.class)
                        .setMaxResults(n)
                        .setReadOnly(true) // hint: read-only for performance
                        .list();
                transaction.commit();
                return new HashSet<>(result);
            } catch (Exception e) {
                System.out.println("Error getting random leaf libraries ");
                e.printStackTrace();
                safeRollback(transaction);
            }
            return Collections.emptySet();
        }
    }

    public void saveLeafLibraries(Set<LeafLibrary> leafLibraries) {
        if (leafLibraries == null || leafLibraries.isEmpty()) return;

        Transaction transaction = null;
        final int batchSize = 50; // tune to your needs (and set hibernate.jdbc.batch_size)
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            int i = 0;
            for (LeafLibrary lib : leafLibraries) {
                session.persist(lib); // assumes new entities (no ID yet)
                System.out.println(i + lib.getGroupId() + ":" + lib.getArtifactId() + ":" + lib.getVersion());
                if (++i % batchSize == 0) {
                    session.flush(); // push to DB
                    session.clear(); // detach to free memory
                }
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
        }
    }


}
