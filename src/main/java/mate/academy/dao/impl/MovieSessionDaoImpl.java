package mate.academy.dao.impl;

import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.List;

@Dao
public class MovieSessionDaoImpl extends AbstractDao implements MovieSessionDao {
    public MovieSessionDaoImpl() {
        super(HibernateUtil.getSessionFactory());
    }

    @Override
    public MovieSession add(MovieSession movieSession) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.persist(movieSession);
            transaction.commit();
            return movieSession;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't add a movie session: " + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public MovieSession get(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(MovieSession.class, id);
        } catch (Exception e) {
            throw new DataProcessingException("Can't get a movie session with id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = sessionFactory.openSession()) {
            Query<MovieSession> getAvailableSessionsQuery = session.createQuery(
                    "from MovieSession ms "
                            + "where ms.id = :movie_id and "
                            + "ms.showTime between :start_time and :end_time",
                    MovieSession.class);
            getAvailableSessionsQuery.setParameter("movie_id", movieId);
            getAvailableSessionsQuery.setParameter("start_time", date.atStartOfDay());
            getAvailableSessionsQuery.setParameter("end_time", date.atTime(23, 59, 59));
            return getAvailableSessionsQuery.getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get a list of movie sessions", e);
        }
    }
}