package org.tudo.managers;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.tudo.Library;
import org.tudo.utils.HibernateUtil;

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
}
