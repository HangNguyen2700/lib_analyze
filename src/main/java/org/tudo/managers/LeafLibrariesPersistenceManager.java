package org.tudo.managers;

import jakarta.persistence.*;
//import jakarta.transaction.Transaction;
import org.hibernate.*;

import org.hibernate.exception.ConstraintViolationException;
import org.tudo.LeafLibrary;
import org.tudo.utils.HibernateUtil;

import java.util.List;
import java.util.Set;

public class LeafLibrariesPersistenceManager {
    private Session session;

    public LeafLibrariesPersistenceManager() {
        session = HibernateUtil.getSessionFactory().openSession();
        System.out.println("New session opened");
        // Close cleanly on Ctrl+C / kill (best-effort)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LeafLibrariesPersistenceManager.this.close();
                System.out.println("### Kill process: Session closed successfully");
            } catch (Exception ignored) {
                System.out.println("### Kill process: Error closing session");
                throw new RuntimeException(ignored);
            }
        }));
    }

    public void close() {
        if (this.session != null && this.session.isOpen()) this.session.close();
        System.out.println("Session closed successfully");
    }

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

    /**
     * Insert (or update) right away.
     * Uses merge() for idempotency in case the row already exists (unique key on G:A:V recommended).
     */
    public void saveLeafLibrary(LeafLibrary leafLibrary) {
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(leafLibrary);
            transaction.commit();
        } catch (ConstraintViolationException dup) {
            System.out.println("Duplicated leaf library found");
            safeRollback(transaction);
        } catch (Exception e) {
            safeRollback(transaction);
            System.out.println("Error saving leaf library ");
            e.printStackTrace();
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

    public LeafLibrary getLeafLibraryById(Long id) {
        return session.find(LeafLibrary.class, id);
    }

    public List<LeafLibrary> getAllLeafLibraries() {
        return session.createQuery("from LeafLibrary", LeafLibrary.class).list();
    }


}
