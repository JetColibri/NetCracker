package com.netcracker.superproject.dao.service;

import com.netcracker.superproject.dao.entity.User;
import com.netcracker.superproject.dao.entity.attributes.Attribute;
import com.netcracker.superproject.dao.entity.attributes.ObjectType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class EntityServiceImpl {

    protected String objectType(Object obj) {
        Class<?> clazz = obj.getClass();
        ObjectType ann = (ObjectType) clazz.getAnnotation(ObjectType.class);
        return ann.type();
    }

    protected Map<String, Object> getAllFields(Object obj) {
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
                method = clazz.getMethod("get" + this.firstUpperCase(String.valueOf(fs[i].getName())));
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

    protected Object setAllFields(String objType, Map<String, Object> allFields) {
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

        for (int i = 0; i < fs.length; ++i) {
            Attribute ann = (Attribute) fs[i].getAnnotation(Attribute.class);
            Iterator var9 = allFields.keySet().iterator();

            while (var9.hasNext()) {
                String st = (String) var9.next();
                if (st.equals(ann.type())) {
                    try {
                        method = clazz.getMethod("set" + this.firstUpperCase(String.valueOf(fs[i].getName())), fs[i].getType());
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

    private String firstUpperCase(String word) {
        return word != null && !word.isEmpty() ? word.substring(0, 1).toUpperCase() + word.substring(1) : "";
    }

    protected void delNull(Map<String, Object> delNull) {
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
}
