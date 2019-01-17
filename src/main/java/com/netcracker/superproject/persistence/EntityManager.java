package com.netcracker.superproject.persistence;

import com.netcracker.superproject.entity.BaseEntity;
import com.netcracker.superproject.entity.annotations.Attribute;
import com.netcracker.superproject.entity.annotations.Entity;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;
import java.sql.*;

public class EntityManager <T extends BaseEntity> {

    Connection conn = Connect.getConnection();
    private static final Logger log = Logger.getLogger(EntityManager.class);

    public BigInteger create(T obj) {
        Map<String, Object> fields = getAllFields(obj);
        delNull(fields);
        BigInteger id = null;

        Statement stmt = null;
        ResultSet rs = null;

        try {

            stmt = conn.prepareStatement("INSERT INTO entity (type_id) VALUES (?) RETURNING id");
            ((PreparedStatement) stmt).setString(1, objectType(obj));
            rs = ((PreparedStatement) stmt).executeQuery();

            if (rs.next()) {
                id = BigInteger.valueOf(rs.getInt(1));
            }
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                stmt = conn.prepareStatement("INSERT INTO value (entity_id, param, value) VALUES (?,?,?)");
                ((PreparedStatement) stmt).setInt(1, id.intValue());
                ((PreparedStatement) stmt).setString(2, entry.getKey());
                ((PreparedStatement) stmt).setString(3, String.valueOf(entry.getValue()));
                ((PreparedStatement) stmt).execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
            return id;
        } finally {
            closeConnect(stmt, rs);
        }
        return id;
    }

    public T read(BigInteger id, Class<T> clazz) {
        Map<String, Object> fields = new HashMap<>();
        Statement stmt = null;
        ResultSet rs = null;

        //BaseEntity
        fields.put("0001", id);

        try {
            stmt = conn.prepareStatement("SELECT * FROM value WHERE entity_id = ?");
            ((PreparedStatement) stmt).setInt(1, id.intValue());
            rs = ((PreparedStatement) stmt).executeQuery();
            while (rs.next()) {
                String param = rs.getString("param");
                Object value = rs.getObject("value");
                fields.put(param, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
            return null;
        } finally {
            closeConnect(stmt, rs);
        }
        return setAllFields(fields, clazz);
    }

    public BigInteger getIdByParam(String param, String value) {
        BigInteger id = null;
        Statement stmt;
        ResultSet rs;

        try {
            stmt = conn.prepareStatement("SELECT v.entity_id FROM value v, attribute a " +
                    "WHERE v.param = a.param AND a.title = ? AND v.value = ? ");
            ((PreparedStatement) stmt).setString(1, param);
            ((PreparedStatement) stmt).setString(2, value);
            rs = ((PreparedStatement) stmt).executeQuery();
            if (rs.next()) {
                id = BigInteger.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
            return id;
        }
        return id;
    }

    public void update(BigInteger id, T obj) {
        Map<String, Object> fields = getAllFields(obj);
        obj.setId(null);
        delNull(fields);
        Statement stmt = null;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                stmt = conn.prepareStatement("UPDATE value SET value = (?) WHERE param = ? AND entity_id = ?");

                ((PreparedStatement) stmt).setString(1, String.valueOf(entry.getValue()));
                ((PreparedStatement) stmt).setString(2, entry.getKey());
                ((PreparedStatement) stmt).setInt(3, id.intValue());
                ((PreparedStatement) stmt).execute();
            } catch (SQLException e) {
                e.printStackTrace();
                log.info(e);
            } finally {
                closeConnect(stmt, null);
            }
        }
    }

    public void delete(BigInteger id) {
        Statement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM entity WHERE id = ?");
            ((PreparedStatement) stmt).execute();
            stmt = conn.prepareStatement("DELETE FROM value WHERE entity_id = ?");
            ((PreparedStatement) stmt).execute();
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
        } finally {
            closeConnect(stmt, null);
        }
    }

    private String objectType(T obj) {
        Class<?> clazz = obj.getClass();
        Entity ann = clazz.getAnnotation(Entity.class);
        return ann.type();
    }

    public Map<String, Object> getAllFields(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Map<String, Object> allFields = new HashMap();
        Method method = null;
        Object value = null;
        Field[] fields = getNameFields(clazz);

        for (Field f : fields) {
            Annotation[] ann2 = f.getAnnotations();
            Attribute ann = f.getAnnotation(Attribute.class);

            try {
                method = clazz.getMethod("get" + firstUpperCase(String.valueOf(f.getName())));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                log.info(e);
                continue;
            }

            try {
                value = method.invoke(obj);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                Map<String, Object> clear = new HashMap();
                log.info(e);
                return clear;
            }
            allFields.put(ann.type(), value);
        }
        return allFields;
    }

    private T setAllFields(Map<String, Object> allFields, Class<T> clazz) {
        Method method = null;
        T obj = null;
        Field[] fields = getNameFields(clazz);
        Object value = null;

        Map<Class, Method> methodMap = new HashMap<>();

        try {
            methodMap.put(Date.class, EntityManager.class.getMethod("convertToDate", Object.class));
            methodMap.put(boolean.class, EntityManager.class.getMethod("convertToBoolean", Object.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            log.info(e);
        }

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            log.info(e);
        }

        obj.setId((BigInteger) allFields.get("0001"));
        for (Map.Entry<String, Object> entry : allFields.entrySet()) {
            for (Field f : fields) {
                Attribute ann = f.getAnnotation(Attribute.class);
                value = entry.getValue();
                if (entry.getKey().equals(ann.type())) {
                    try {
                        method = clazz.getMethod("set" + firstUpperCase(String.valueOf(f.getName())), f.getType());
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                        log.info(e);
                    }
                    try {
                        if (methodMap.containsKey(f.getType()) && value != null) {
                            try {
                                value = methodMap.get(f.getType()).invoke(null, value);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                                log.info(e);
                            }
                        }
                        method.invoke(obj, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                        log.info(e);
                    }
                }
            }
        }
        return obj;
    }

    private String firstUpperCase(String word) {
        return word != null && !word.isEmpty() ? word.substring(0, 1).toUpperCase() + word.substring(1) : "";
    }

    private void delNull(Map<String, Object> delNull) {
        Iterator iterD = delNull.entrySet().iterator();

        Object ob;
        while (iterD.hasNext()) {
            Entry<String, Object> ent = (Entry) iterD.next();
            ob = ent.getValue();
            if (ob == null) {
                iterD.remove();
            }
        }

        Iterator var5 = delNull.entrySet().iterator();

        while (var5.hasNext()) {
            ob = var5.next();
        }
    }

    public void dropTables() {
        try {
            conn.prepareStatement("DROP TABLE type;").execute();
            conn.prepareStatement("DROP TABLE entity;").execute();
            conn.prepareStatement("DROP TABLE attribute;").execute();
            conn.prepareStatement("DROP TABLE value;").execute();
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
        }
    }

    public void createTables() {
        try {
            conn.prepareStatement("CREATE TABLE type (id serial, title text);").execute();
            conn.prepareStatement("INSERT INTO type(title) VALUES ('user');").execute();
            conn.prepareStatement("INSERT INTO type(title) VALUES ('event');").execute();

            conn.prepareStatement("CREATE TABLE entity(id serial, type_id text);").execute();

            conn.prepareStatement("CREATE TABLE attribute (id serial, type_id integer, param text, title text);").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '0001', 'id');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1001', 'email');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1002', 'password');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1003', 'role');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1004', 'firstName');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1005', 'lastName');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1006', 'location');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1007', 'birthdayDate');").execute();
            conn.prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1008', 'registrationDate');").execute();
            conn.prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1009', 'photo');").execute();
            conn.prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1010', 'tmpEmail');").execute();
            conn.prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1011', 'token');").execute();

            conn.prepareStatement("CREATE TABLE value (id serial, entity_id integer, param text, value text);").execute();

            conn.prepareStatement("CREATE TABLE reference (id serial, title text, parent_id integer, entity_id integer)").execute();
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
        }
    }

    private Field[] getNameFields(Class<T> clazz) {
        Field[] parentFs;
        Field[] childFs;
        Field[] fields = null;
        Class<?> parentClazz = BaseEntity.class;
        try {
            parentFs = Class.forName(parentClazz.getName()).getDeclaredFields();
            childFs = Class.forName(clazz.getName()).getDeclaredFields();

            fields = new Field[parentFs.length + childFs.length];
            System.arraycopy(parentFs, 0, fields, 0, parentFs.length);
            System.arraycopy(childFs, 0, fields, parentFs.length, childFs.length);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            log.info(e);
        }
        return fields;
    }

    public static Date convertToDate(Object obj) {
        SimpleDateFormat format = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy",
                Locale.ENGLISH);
        java.util.Date date = null;
        try {
            date = format.parse(String.valueOf(obj));
        } catch (ParseException e) {
            e.printStackTrace();
            log.info(e);
        }
        return date;
    }

    public static boolean convertToBoolean(Object obj) {
        boolean bool = Boolean.parseBoolean(String.valueOf(obj));
        return bool;
    }

    private void closeConnect(Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
        }
    }
}