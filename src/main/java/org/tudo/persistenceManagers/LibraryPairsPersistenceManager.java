package org.tudo.persistenceManagers;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.tudo.LibraryPair;
import org.tudo.utils.HibernateUtil;

public class LibraryPairsPersistenceManager {
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

    public void save(LibraryPair libraryPair) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.merge(libraryPair);
                transaction.commit();
            } catch (Exception e) {
                System.err.println("Error saving library pair");
                safeRollback(transaction);
                e.printStackTrace();
            }
        }
    }
}
