package com.netcracker.superproject.persistence;

import com.netcracker.superproject.entity.BaseEntity;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.entity.annotations.Attribute;
import com.netcracker.superproject.entity.annotations.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.*;

public class EntityManager <T extends BaseEntity> {

    public BigInteger create(T obj) {
        Map<String, Object> fields = getAllFields(obj);
        delNull(fields);
        BigInteger id = null;

        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = connect();
            stmt = conn.prepareStatement("INSERT INTO entity (type_id) VALUES (?) RETURNING id");
            ((PreparedStatement) stmt).setString(1, objectType(obj));
            rs = ((PreparedStatement) stmt).executeQuery();

            if(rs.next()){
                id = BigInteger.valueOf(rs.getInt(1));
            }
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                    stmt = conn.prepareStatement("INSERT INTO value (entity_id, param, value) VALUES (?,?,?)");
                    ((PreparedStatement) stmt).setInt(1, id.intValue());
                    ((PreparedStatement) stmt).setString(2, entry.getKey());
                    ((PreparedStatement) stmt).setString(3, (String) entry.getValue());
                    ((PreparedStatement) stmt).execute();
            }
        } catch (SQLException e) {

        } finally {
          closeConnect(stmt, rs, conn);
        }
        return id;
    }

    public T read(BigInteger id, Class<T> clazz) {
        Map<String, Object> fields = new HashMap<>();
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        //BaseEntity
        fields.put("0001", id);

        try {
            conn = connect();
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
        }finally {
            closeConnect(stmt, rs, conn);
        }
        return setAllFields(fields, clazz);
    }

    public BigInteger getIdByParam(String param, String value) {
        BigInteger id = null;
        Statement stmt;
        ResultSet rs;
        Connection conn;

        try {
            conn = connect();
            stmt = conn.prepareStatement("SELECT v.entity_id FROM value v, attribute a " +
                    "WHERE v.param = a.param AND a.title = ? AND v.value = ? ");
            ((PreparedStatement) stmt).setString(1, param);
            ((PreparedStatement) stmt).setString(2, value);
            rs = ((PreparedStatement) stmt).executeQuery();
            if(rs.next()){
                id = BigInteger.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void update(BigInteger id, T obj) {
        Map<String, Object> fields = getAllFields(obj);
        obj.setId(null);
        delNull(fields);
        Statement stmt = null;
        Connection conn = null;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                conn = connect();
                stmt = conn.prepareStatement("UPDATE value SET value = (?) WHERE param = ? AND entity_id = ?");

                ((PreparedStatement) stmt).setString(1, String.valueOf(entry.getValue()));
                ((PreparedStatement) stmt).setString(2, entry.getKey());
                ((PreparedStatement) stmt).setInt(3, id.intValue());
                ((PreparedStatement) stmt).execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }finally {
                closeConnect(stmt, null, conn);
            }
        }
    }

    public void delete(BigInteger id) {
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = connect();
            stmt = conn.prepareStatement("DELETE FROM entity WHERE id = ?");
            ((PreparedStatement) stmt).execute();
            stmt = conn.prepareStatement("DELETE FROM value WHERE entity_id = ?");
            ((PreparedStatement) stmt).execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            closeConnect(stmt, null, conn);
        }
    }

    private String objectType(T obj) {
        Class<?> clazz = obj.getClass();
        Entity ann = clazz.getAnnotation(Entity.class);
        return ann.type();
    }

    private Map<String, Object> getAllFields(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Map<String, Object> allFields = new HashMap();
        Method method = null;
        Object value = null;
        Field[] fields = getNameFields(clazz);

        for (Field f : fields) {
            try {
                method = clazz.getMethod("get" + firstUpperCase(String.valueOf(f.getName())));
            } catch (NoSuchMethodException ex) {
                System.out.println("Ошибочка 4");
            }

            try {
                value = method.invoke(obj);
            } catch (IllegalAccessException ex) {
                System.out.println("Ошибочка 5.1");
            } catch (InvocationTargetException ex) {
                System.out.println("Ошибочка 5.2");
            }
            Attribute ann = f.getAnnotation(Attribute.class);
            allFields.put(ann.type(), value);
        }
        return allFields;
    }

    private T setAllFields(Map<String, Object> allFields, Class<T> clazz) {
        Method method = null;
        T obj = null;
        Field[] fields = getNameFields(clazz);

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        for (Map.Entry<String, Object> entry : allFields.entrySet()) {
            for (Field f : fields) {
                Attribute ann = f.getAnnotation(Attribute.class);
                if(entry.getKey().equals(ann.type())){
                    try {
                        method = clazz.getMethod("set" + firstUpperCase(String.valueOf(f.getName())), f.getType());
                    } catch (NoSuchMethodException ex) {
                        System.out.println("Ошибочка 7");
                    }

                    try {
                        method.invoke(obj, entry.getValue());
                    } catch (IllegalAccessException var12) {
                        System.out.println("Ошибочка 7.1");
                    } catch (InvocationTargetException var13) {
                        System.out.println("Ошибочка 7.2");
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
            System.out.println(ob);
        }
    }

    public void dropTables() {
        try {
            connect().prepareStatement("DROP TABLE type;").execute();
            connect().prepareStatement("DROP TABLE entity;").execute();
            connect().prepareStatement("DROP TABLE attribute;").execute();
            connect().prepareStatement("DROP TABLE value;").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        try {
            connect().prepareStatement("CREATE TABLE type (id serial, title text);").execute();
            connect().prepareStatement("INSERT INTO type(title) VALUES ('user');").execute();
            connect().prepareStatement("INSERT INTO type(title) VALUES ('event');").execute();

            connect().prepareStatement("CREATE TABLE entity(id serial, type_id text);").execute();

            connect().prepareStatement("CREATE TABLE attribute (id serial, type_id integer, param text, title text);").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '0001', 'id');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1001', 'email');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1002', 'password');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1003', 'role');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1004', 'firstName');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1005', 'lastName');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1006', 'location');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1007', 'birthdayDate');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1008', 'registrationDate');").execute();
            connect().prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1009', 'photo');").execute();
            connect().prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1010', 'tmpEmail');").execute();
            connect().prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1011', 'token');").execute();

            connect().prepareStatement("CREATE TABLE value (id serial, entity_id integer, param text, value text);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Field[] getNameFields(Class<T> clazz){
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
        } catch (ClassNotFoundException ex) {
            System.out.println("Ошибочка 3");
        }
        return fields;
    }
    private Connection connect() {
        // JDBC URL, username and password of postgres server
        final String url = "jdbc:postgresql://localhost:5433/postgres";
        final String user = "postgres";
        final String password = "";
        Connection conn = null;

        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return conn;
    }

    private void closeConnect(Statement stmt, ResultSet rs, Connection conn){
        try {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
        }
    }
}