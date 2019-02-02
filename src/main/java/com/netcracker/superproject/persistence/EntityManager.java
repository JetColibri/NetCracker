package com.netcracker.superproject.persistence;

import com.netcracker.superproject.entity.BaseEntity;
import com.netcracker.superproject.entity.annotations.Attribute;
import com.netcracker.superproject.entity.annotations.Entity;
import com.netcracker.superproject.entity.annotations.Reference;
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
import java.sql.*;

public class EntityManager <T extends BaseEntity> {

    Connection conn = Connect.getConnection();
    private static final Logger log = Logger.getLogger(EntityManager.class);

    public BigInteger create(T obj) {
        Map<String, Object> fields = getAllFields(obj);
        delNull(fields);
        BigInteger id = null;

        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.prepareStatement("INSERT INTO entity (type_id) VALUES (?) RETURNING id");
            stmt.setString(1, objectType(obj));
            rs = stmt.executeQuery();

            if (rs.next()) {
                id = BigInteger.valueOf(rs.getInt(1));
            }


            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                if(entry.getKey().equals("reference")){
                    continue;
                }
                stmt = conn.prepareStatement("INSERT INTO value (entity_id, param, value) VALUES (?,?,?)");
                stmt.setInt(1, id.intValue());
                stmt.setString(2, entry.getKey());
                stmt.setString(3, String.valueOf(entry.getValue()));
                stmt.execute();
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
        PreparedStatement stmt = null;
        ResultSet rs = null;

        //BaseEntity
        fields.put("0001", id);

        try {
            stmt = conn.prepareStatement("SELECT * FROM value WHERE entity_id = ?");
            stmt.setInt(1, id.intValue());
            rs = stmt.executeQuery();
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
        PreparedStatement stmt;
        ResultSet rs;

        try {
            stmt = conn.prepareStatement("SELECT v.entity_id FROM value v, attribute a " +
                    "WHERE v.param = a.param AND a.title = ? AND v.value = ? ");
            stmt.setString(1, param);
            stmt.setString(2, value);
            rs = stmt.executeQuery();
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
        PreparedStatement stmt = null;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            try {
                if(entry.getKey().equals("reference")){
                    continue;
                }
                stmt = conn.prepareStatement("UPDATE value SET value = (?) WHERE param = ? AND entity_id = ?");

                stmt.setString(1, String.valueOf(entry.getValue()));
                stmt.setString(2, entry.getKey());
                stmt.setInt(3, id.intValue());
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                log.info(e);
            } finally {
                closeConnect(stmt, null);
            }
        }
    }

    public void delete(BigInteger id) {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("DELETE FROM entity WHERE id = ?");
            stmt.execute();
            stmt = conn.prepareStatement("DELETE FROM value WHERE entity_id = ?");
            stmt.execute();
            stmt = conn.prepareStatement("DELETE FROM reference WHERE parent_id = ?");
            stmt.execute();
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
        Map<String, ArrayList> arrayRef = new HashMap();
        Method method = null;
        Object value = null;
        Field[] fields = getNameFields(clazz);

        for (Field f : fields) {
            Reference ref = f.getAnnotation(Reference.class);
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
            if(ref != null){
                arrayRef.put(f.getName(), (ArrayList) value);
            } else {
                allFields.put(ann.type(), value);
            }
        }
        allFields.put("reference", arrayRef);
        return allFields;
    }

    public T setAllFields(Map<String, Object> allFields, Class<T> clazz) {
        Method method = null;
        T obj = null;
        Field[] fields = getNameFields(clazz);
        Object value;
        Map<String, ArrayList> refMap = new HashMap<>();
        Map<Class, Method> methodMap = new HashMap<>();

        if(allFields.containsKey("reference")){
            refMap = (Map<String, ArrayList>) allFields.get("reference");
        }
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

        for (Field f : fields) {
            for (Map.Entry<String, Object> entry : allFields.entrySet()) {
                String fieldName = f.getName();
                Class fieldType = f.getType();
                value = entry.getValue();

                Reference ref = f.getAnnotation(Reference.class);
                Attribute atr = f.getAnnotation(Attribute.class);

                if(ref != null){
                    value = refMap.getOrDefault(fieldName, null);
                } else if (!entry.getKey().equals(atr.type())) {
                    continue;
                }

                try {
                    method = clazz.getMethod("set" + firstUpperCase(fieldName), fieldType);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                    log.info(e);
                }
                try {
                    if (methodMap.containsKey(fieldType) && value != null) {
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
        return obj;
    }
    public List<T> getSomeEntities(Class<T> clazz, int firstEntity, int totalEntities){
        PreparedStatement stmt;
        ResultSet rs;
        Entity type_id = clazz.getAnnotation(Entity.class);

        try {
            stmt = conn.prepareStatement("WITH entities AS (SELECT id FROM entity  WHERE type_id = ? LIMIT ? ) \n" +
                    "SELECT v.entity_id, v.param, v.value\n" +
                    " FROM value AS v JOIN entities ON entities.id = v.entity_id WHERE entity_id >= ? ORDER BY entity_id;");
            stmt.setString(1,  type_id.type());
            stmt.setInt(2, totalEntities);
            stmt.setInt(3, firstEntity);
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return getEntities(rs, clazz);
    }

    public List<T> getSomeEntitiesByParam(Class<T> clazz, int firstEntity, int totalEntities, Map<String, String> map){
        PreparedStatement stmt;
        ResultSet rs;
        int i = 0;

        StringBuilder sql = new StringBuilder("WITH entities AS (SELECT entity_id FROM value WHERE" );
        for (Map.Entry<String, String> param : map.entrySet()){
            if(i != 0){
                sql.append("AND");
            }
            sql.append("entity_id IN (SELECT entity_id FROM value WHERE param = ");
            sql.append(param.getKey());
            sql.append("AND value = ");
            sql.append(param.getValue());
            sql.append(")");
            i++;
        }
        sql.append("AND entity_id > ");
        sql.append(firstEntity);
        sql.append("GROUP BY entity_id ORDER BY entity_id LIMIT ");
        sql.append(totalEntities);
        sql.append("SELECT v.entity_id, v.param, v.value\n" +
                "FROM value AS v JOIN entities ON entities.entity_id = v.entity_id;");

        try {
            stmt = conn.prepareStatement(String.valueOf(sql));
            rs = stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return getEntities(rs, clazz);
    }

    private List<T> getEntities(ResultSet rs, Class<T> clazz){
        String tmp = "";
        Map<String, Object> entityFields = new HashMap<>();
        List<T> objects = new ArrayList<>();

   try {
       while (rs.next()) {
           if (String.valueOf(tmp).equals(rs.getString("entity_id"))) {
               if (tmp != "") {
                   objects.add(setAllFields(entityFields, clazz));
               }
               tmp = rs.getString("entity_id");
               entityFields.clear();
               entityFields.put("0001", new BigInteger(tmp));
               entityFields.put("reference", getReference(new BigInteger(tmp)));

           } else {
               entityFields.put(rs.getString("param"), rs.getString("value"));
           }
           if (rs.isLast()) {
               objects.add(setAllFields(entityFields, clazz));
           }
       }
   }catch (SQLException e){

   }
        return objects;
    }

    public Map<String, ArrayList> getReference(BigInteger id) throws SQLException {
        PreparedStatement stmt;
        Map<String, ArrayList> references = new HashMap<>();
        ArrayList list = new ArrayList<>();
        String tmp = "";
        stmt = conn.prepareStatement("SELECT title, entity_id FROM reference WHERE parent_id = ?");
        stmt.setString(1, String.valueOf(id));
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            if(!tmp.equals(rs.getString("title"))){
                if(!tmp.equals("")){
                   references.put(tmp, list);
                }
                tmp = rs.getString("title");
                list.add(rs.getString("entity_id"));

            }else{
                list.add(rs.getString("entity_id"));
            }
            if(rs.isLast()){
                references.put(tmp, list);
            }
        }
        return references;
    }

    public void addReference(String title, BigInteger parent_id, BigInteger entity_id){
        
        try {
            System.out.println("+");
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO reference (title, parent_id, entity_id) VALUES (?, ?, ?);");
            stmt.setString(1, title);
            stmt.setString(2, String.valueOf(parent_id));
            stmt.setString(3, String.valueOf(entity_id));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
    private String firstUpperCase(String word) {
        return word != null && !word.isEmpty() ? word.substring(0, 1).toUpperCase() + word.substring(1) : "";
    }

    private void delNull(Map<String, Object> delNull) {
        delNull.entrySet().removeIf(ent -> ent.getValue() == null);
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
        return Boolean.parseBoolean(String.valueOf(obj));
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

    public void dropTables() {
        try {
            conn.prepareStatement("DROP TABLE type;").execute();
            conn.prepareStatement("DROP TABLE entity;").execute();
            conn.prepareStatement("DROP TABLE attribute;").execute();
            conn.prepareStatement("DROP TABLE value;").execute();
            conn.prepareStatement("DROP TABLE reference;").execute();
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

            conn.prepareStatement("CREATE TABLE reference (id serial, title text, parent_id text, entity_id text)").execute();
        } catch (SQLException e) {
            e.printStackTrace();
            log.info(e);
        }
    }
}