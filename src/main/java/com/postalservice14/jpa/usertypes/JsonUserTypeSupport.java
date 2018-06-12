package com.postalservice14.jpa.usertypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.IOException;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

public abstract class JsonUserTypeSupport implements UserType, DynamicParameterizedType {

    private static final int[] SQL_TYPES = {Types.JAVA_OBJECT};
    private Class<?> returnedClass;

    @Override
    public void setParameterValues(Properties properties) {
        final ParameterType reader = (ParameterType) properties.get(PARAMETER_TYPE);

        if (reader != null) {
            this.returnedClass = reader.getReturnedClass();
        }
    }

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> returnedClass() {
        return this.returnedClass;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        if (x == y) {
            return true;
        } else if (x == null || y == null) {
            return false;
        } else {
            return x.equals(y);
        }
    }

    @Override
    public int hashCode(Object x) throws HibernateException {
        return null == x ? 0 : x.hashCode();
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner) throws HibernateException, SQLException {
        Object result = rs.getObject(names[0]);
        if (result instanceof PGobject) {
            return convertJsonToObject(((PGobject) result).getValue());
        }

        return null;
    }

    private Object convertJsonToObject(String content) {
        if ((content == null) || (content.isEmpty())) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            JavaType type = createJavaType(mapper);
            if (type == null) {
                return mapper.readValue(content, returnedClass);
            }

            return mapper.readValue(content, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected JavaType createJavaType(ObjectMapper mapper) {
        try {
            return mapper.getTypeFactory().constructSimpleType(returnedClass(), null);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        PGobject dataObject = new PGobject();
        dataObject.setType(getType());

        if (value != null) {
            dataObject.setValue(convertObjectToJson(value));
        }

        statement.setObject(index, dataObject);
    }

    protected abstract String getType();

    private String convertObjectToJson(Object object) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        String json = convertObjectToJson(value);
        return convertJsonToObject(json);
    }

    @Override
    public boolean isMutable() {
        return true;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) deepCopy(value);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
