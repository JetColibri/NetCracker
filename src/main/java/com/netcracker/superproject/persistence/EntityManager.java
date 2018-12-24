package com.netcracker.superproject.persistence;

import com.netcracker.superproject.entity.BaseEntity;
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

            rs.next();
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


    public Object read(BigInteger id, Class clazz) {
        Map<String, Object> fields = new HashMap<>();
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
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
        delNull(fields);
        Statement stmt = null;
        Connection conn = null;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                conn = connect();
                stmt = conn.prepareStatement("UPDATE value SET value = (?) WHERE param = ? AND entity_id = ?");
                ((PreparedStatement) stmt).setString(1, (String) entry.getValue());
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
        Entity ann = (Entity) clazz.getAnnotation(Entity.class);
        return ann.type();
    }

    private Map<String, Object> getAllFields(T obj) {
        Field[] fs = null;
        Class<?> clazz = obj.getClass();
        Map<String, Object> allFields = new HashMap();
        Method method = null;
        Object value = null;

        try {
            Class c = Class.forName(clazz.getName());
            fs = c.getDeclaredFields();
        } catch (ClassNotFoundException var12) {
            System.out.println("Ошибочка 3");
        }

        for (int i = 0; i < fs.length; ++i) {
            try {
                method = clazz.getMethod("get" + firstUpperCase(String.valueOf(fs[i].getName())));
            } catch (NoSuchMethodException var11) {
                System.out.println("Ошибочка 4");
            }

            try {
                value = method.invoke(obj);
            } catch (IllegalAccessException var9) {
                System.out.println("Ошибочка 5.1");
            } catch (InvocationTargetException var10) {
                System.out.println("Ошибочка 5.2");
            }
            Attribute ann = (Attribute) fs[i].getAnnotation(Attribute.class);
            allFields.put(ann.type(), value);
        }

        return allFields;
    }

    private Object setAllFields(Map<String, Object> allFields, Class<T> clazz) {
        Field[] fs = null;
        Method method = null;

        T obj = null;

        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            Class c = Class.forName(clazz.getName());
            fs = c.getDeclaredFields();
        } catch (ClassNotFoundException var15) {
            System.out.println("Ошибочка 6");
        }

        for (int i = 0; i < fs.length; i++) {
            Attribute ann = (Attribute) fs[i].getAnnotation(Attribute.class);
            Iterator var9 = allFields.keySet().iterator();

            while (var9.hasNext()) {
                String st = (String) var9.next();
                if (st.equals(ann.type())) {
                    try {
                        method = clazz.getMethod("set" + firstUpperCase(String.valueOf(fs[i].getName())), fs[i].getType());
                    } catch (NoSuchMethodException var14) {
                        System.out.println("Ошибочка 7");
                    }

                    try {
                        method.invoke(obj, allFields.get(st));
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
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1001', 'email');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1002', 'password');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1003', 'role');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1004', 'firstName');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1005', 'lastName');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1006', 'location');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1007', 'birthdayDate');").execute();
            connect().prepareStatement("INSERT INTO attribute (type_id, param, title) VALUES (1, '1008', 'registrationDate');").execute();
            connect().prepareStatement("INSERT INTO attribute( type_id, param, title) VALUES (1, '1009', 'photo');").execute();

            connect().prepareStatement("CREATE TABLE value (id serial, entity_id integer, param text, value text);").execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection connect() {
        // JDBC URL, username and password of postgres server
        final String url = "jdbc:postgresql://localhost:5432/postgres";
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