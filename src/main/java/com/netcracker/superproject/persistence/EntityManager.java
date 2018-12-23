package com.netcracker.superproject.persistence;

import com.netcracker.superproject.entity.BaseEntity;
import com.netcracker.superproject.entity.User;
import com.netcracker.superproject.entity.annotations.Attribute;
import com.netcracker.superproject.entity.annotations.Entity;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.sql.*;

public class EntityManager <T extends BaseEntity> {

    // JDBC URL, username and password of postgres server
    private static final String url = "jdbc:postgresql://localhost:5432/project";
    private static final String user = "postgres";
    private static final String password = "epifanik";

    // JDBC variables for opening and managing connection
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;

    public BigInteger create(T obj) {
        Map<String, Object> fields = getAllFields(obj);
        delNull(fields);
        BigInteger id = null;
        try {
            // opening database connection to postgres server
            con = DriverManager.getConnection(url, user, password);

            // getting Statement object to execute query
            stmt = con.createStatement();


            rs = stmt.executeQuery(queryInsertObject(obj));
            rs.next();
            id = BigInteger.valueOf(rs.getInt(1));
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                stmt.executeUpdate(queryInsertParam(id, entry.getKey(), String.valueOf(entry.getValue())));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return id;
    }

    //temporarily only for "User"
    public User read(BigInteger id) {
        Map<String, Object> fields = new HashMap<>();
        connect();
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM value WHERE entity_id = '" + id + "'");
            while (rs.next()) {
                String param = rs.getString("param");
                Object value = rs.getObject("value");
                fields.put(param, value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return setAllFields(fields);
    }

    public BigInteger getIdByParam(String param, String value) {
        BigInteger id = null;
        Map<String, Object> fields = new HashMap<>();
        connect();
        try {
        String[][] arr = createTable("SELECT v.entity_id FROM value v, attribute a WHERE v.param = a.param AND a.title = '" + param + "' AND v.value = '" + value + "'");
        id = BigInteger.valueOf(Integer.parseInt(arr[1][0]));
        } catch (SQLException | InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return id;
    }

    public void update(BigInteger id, T obj) {
        Map<String, Object> fields = getAllFields(obj);
        delNull(fields);
        connect();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                stmt.executeUpdate(queryUpdateParam(id, entry.getKey(), String.valueOf(entry.getValue())));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void delete(BigInteger id) {
        connect();
        try {
            stmt.executeUpdate("DELETE FROM entity WHERE id = '" + id + "'");
            stmt.executeUpdate("DELETE FROM value WHERE entity_id = '" + id + "'");
        } catch (SQLException e) {
            e.printStackTrace();
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

    private User setAllFields(Map<String, Object> allFields) {
        User user = new User();
        Field[] fs = null;
        Class<?> clazz = User.class;
        Method method = null;

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
                        method.invoke(user, allFields.get(st));
                    } catch (IllegalAccessException var12) {
                        System.out.println("Ошибочка 7.1");
                    } catch (InvocationTargetException var13) {
                        System.out.println("Ошибочка 7.2");
                    }
                }
            }
        }
        return user;
    }

    private String queryInsertObject(T obj) {
        String typeId = objectType(obj);
        String sql = "INSERT INTO entity (type_id) VALUES ('" + typeId + " ') RETURNING id";
        return sql;
    }

    private String queryInsertParam(BigInteger id, String param, String val) {
        String value = "";
        value += "'" + id + "'";
        value += ",";
        value += "'" + param + "'";
        value += ",";
        value += "'" + val + "'";
        return "INSERT INTO value (entity_id, param, value) VALUES (" + value + ");";
    }

    private String queryUpdateParam(BigInteger id, String param, String value) {
        return "UPDATE value SET value = ('" + value + "') WHERE param = '" + param + "' AND entity_id = '" + id + "'";
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

    public void connect() {
        try {
            con = DriverManager.getConnection(url, user, password);
            stmt = con.createStatement();
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }

    public String[][] createTable(String sql) throws ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet resultSet = statement.executeQuery(sql);
        int col = resultSet.getMetaData().getColumnCount();
        resultSet.last();
        int row = resultSet.getRow();
        resultSet.first();
        String[][] mass = new String[row + 1][col];
        int j = 0;
        for (int i = 1; i <= col; i++)
            mass[j][i - 1] = resultSet.getMetaData().getColumnName(i);

        resultSet.beforeFirst();
        while (resultSet.next()) {
            j++;
            for (int i = 1; i <= col; i++) {
                mass[j][i - 1] = resultSet.getString(i);
            }
        }
        statement.closeOnCompletion();
        return mass;
    }
}