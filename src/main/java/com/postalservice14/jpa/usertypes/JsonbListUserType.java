package com.postalservice14.jpa.usertypes;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.UserCollectionType;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonbListUserType extends JsonbUserType implements UserCollectionType {
    @Override
    protected JavaType createJavaType(ObjectMapper mapper) {
        return mapper.getTypeFactory().constructCollectionType(List.class, returnedClass());
    }

    @Override
    public PersistentCollection instantiate(SharedSessionContractImplementor session, CollectionPersister persister) throws HibernateException {
        return new PersistentList(session);
    }

    @Override
    public PersistentCollection wrap(SharedSessionContractImplementor session, Object collection) {
        return new PersistentList(session, (List<?>) collection);
    }

    @Override
    public Iterator getElementsIterator(Object collection) {
        return cast(collection).iterator();
    }

    private PersistentList cast(Object collection) {
        return (PersistentList) collection;
    }

    @Override
    public boolean contains(Object collection, Object entity) {
        return cast(collection).contains(entity);
    }

    @Override
    public Object indexOf(Object collection, Object entity) {
        return cast(collection).indexOf(entity);
    }

    @Override
    public Object replaceElements(Object original, Object target, CollectionPersister persister, Object owner, Map copyCache, SharedSessionContractImplementor session) throws HibernateException {
        PersistentList originalList = cast(original);
        PersistentList targetList = cast(target);
        targetList.clear();
        targetList.addAll(originalList);

        return target;
    }

    @Override
    public Object instantiate(int anticipatedSize) {
        return new PersistentList();
    }
}
