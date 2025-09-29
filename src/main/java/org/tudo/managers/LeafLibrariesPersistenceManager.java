package org.tudo.managers;

import jakarta.persistence.*;
//import jakarta.transaction.Transaction;
import org.hibernate.*;

import org.tudo.LeafLibrary;
import org.tudo.utils.HibernateUtil;

import java.util.List;
import java.util.Set;

public class LeafLibrariesPersistenceManager {

    public void saveLeafLibrary(LeafLibrary leafLibrary) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(leafLibrary);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
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
                System.out.println(i + lib.getGroupId() + ":" + lib.getArtifactId() + ":" + lib.getBaseVersion());
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


    public void updateLeafLibrary(LeafLibrary leafLibrary) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(leafLibrary);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public LeafLibrary getLeafLibraryById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(LeafLibrary.class, id);
        }
    }

    public List<LeafLibrary> getAllLeafLibraries() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from LeafLibrary", LeafLibrary.class).list();
        }
    }



//    public static void insert() {
//        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("analysisPU");
//        EntityManager entityManager = entityManagerFactory.createEntityManager();
//        EntityTransaction transaction = entityManager.getTransaction();
//
//        try {
//            transaction.begin();
//            LeafLibrary leaf = new LeafLibrary();
//            leaf.setGroupId("org.example");
//            leaf.setArtifactId("demo-lib");
//            leaf.setBaseVersion("1.0.0");
//            leaf.setClassifier(null);
//
//            entityManager.persist(leaf);          // INSERT
//            transaction.commit();
//            System.out.println("Inserted id = " + leaf.getId());
//        } catch (Exception e) {
//            if (transaction.isActive()) transaction.rollback();
//            throw e;
//        } finally {
//            entityManager.close();
//            entityManagerFactory.close();
//        }
//    }

}
