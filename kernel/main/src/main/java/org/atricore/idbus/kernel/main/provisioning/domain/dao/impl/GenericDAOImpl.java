package org.atricore.idbus.kernel.main.provisioning.domain.dao.impl;

import org.atricore.idbus.kernel.main.provisioning.domain.dao.GenericDAO;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

/**
 * @author <a href=mailto:sgonzalez@atricor.org>Sebastian Gonzalez Oyuela</a>
 */
public class GenericDAOImpl<T> implements GenericDAO<T> {

    protected PersistenceManagerFactory pmf;
    protected PersistenceManager pm;

    public GenericDAOImpl() {
        persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public GenericDAOImpl(PersistenceManagerFactory pmf) {
        this();
        this.pmf = pmf;
        this.pm = pmf.getPersistenceManager();
    }

    public PersistenceManager getPersistenceManager() {
        return pm;
    }

    public void setPmf(PersistenceManagerFactory pmf) {
        this.pmf = pmf;
    }

    private Class<T> persistentClass;

    public T createObject(T object) {
        return getPersistenceManager().makePersistent(object);
    }

    public void deleteObject(T object) {
        getPersistenceManager().deletePersistent(object);
    }

    public T findObjectById(Serializable id) {
        return (T) getPersistenceManager().getObjectById(persistentClass, id);
    }

    public void flush() {
        getPersistenceManager().flush();
    }


    public T updateObject(T object) {
        return getPersistenceManager().makePersistent(object);
    }

    public Collection<T> findAll() {
        Collection<T> results = (Collection<T>) getPersistenceManager().newQuery(persistentClass).execute();
        return results;

    }
}