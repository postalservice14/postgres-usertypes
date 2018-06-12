package com.postalservice14.jpa.usertypes;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.DynamicParameterizedType;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Properties;

public class XmlUserTypeSupport implements UserType, DynamicParameterizedType {

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
            return convertXmlToObject(((PGobject) result).getValue());
        }

        return null;
    }

    private Object convertXmlToObject(String content) {
        if ((content == null) || (content.isEmpty())) {
            return null;
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(returnedClass);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(content);
            return jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement statement, Object value, int index, SharedSessionContractImplementor session) throws HibernateException, SQLException {
        PGobject dataObject = new PGobject();
        dataObject.setType(getType());

        if (value != null) {
            dataObject.setValue(convertObjectToXml(value));
        }

        statement.setObject(index, dataObject);
    }

    private String getType() {
        return "xml";
    }

    private String convertObjectToXml(Object object) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(returnedClass);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

            StringWriter writer = new StringWriter();
            jaxbMarshaller.marshal(object, writer);

            return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (value == null) {
            return null;
        }

        String xml = convertObjectToXml(value);
        return convertXmlToObject(xml);
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
