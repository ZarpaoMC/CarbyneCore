package com.medievallords.carbyne.database;

import com.google.common.primitives.Primitives;
import com.medievallords.carbyne.Carbyne;
import com.medievallords.carbyne.database.annotations.CollectionName;
import com.medievallords.carbyne.database.annotations.DatabaseSerializer;
import com.medievallords.carbyne.database.annotations.MongoColumn;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang.ClassUtils;
import org.bson.Document;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class MongoInitializer {

    public void update() {
        if (!this.getClass().isAnnotationPresent(CollectionName.class)) {
            Bukkit.getLogger().log(Level.SEVERE, "CollectionName Class not found while using MongoInitializer (" + this.getClass().getSimpleName() + ")");
            return;
        }

        String tableName = this.getClass().getAnnotation(CollectionName.class).name();

        MongoCollection<Document> col = Carbyne.getInstance().getMongoDatabase().getCollection(tableName);

        String identifier = null;
        Object identifierValue = null;
        HashMap<String, Object> values = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            MongoColumn column = field.getAnnotation(MongoColumn.class);

            if (column != null) {
                if (column.identifier()) {
                    identifier = column.name();
                    identifierValue = getValue(field);
                } else {
                    values.put(column.name(), getValue(field));
                }
            }
        }

        if (identifier == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Identifier not found while using MongoInitializer (" + this.getClass().getSimpleName() + ")");
            return;
        }

        Document doc = new Document(identifier, identifierValue);
        BasicDBObject searchQuery = new BasicDBObject().append(identifier, identifierValue);

        doc.putAll(values);

        if (documentExists(searchQuery, col)) {
            col.updateOne(searchQuery, new Document("$set", doc));
        } else {
            col.insertOne(doc);
        }
    }

    public static List<MongoInitializer> select(BasicDBObject search, Class<? extends MongoInitializer> type) {
        List<MongoInitializer> vals = new ArrayList<>();
        CollectionName collectionName = type.getAnnotation(CollectionName.class);

        if (collectionName == null) {
            Bukkit.getLogger().log(Level.SEVERE, "CollectionName Class not found while using MongoInitializer (" + type.getSimpleName() + ")");
            return vals;
        }

        MongoCollection<Document> col = Carbyne.getInstance().getMongoDatabase().getCollection(collectionName.name());

        for (Document doc : col.find(search)) {
            try {
                MongoInitializer mongo = type.newInstance();

                for (Field field : type.getDeclaredFields()) {
                    MongoColumn mongoColumn = field.getAnnotation(MongoColumn.class);
                    if (mongoColumn != null) {
                        Object value = doc.get(mongoColumn.name());
                        if (value != null) {
                            mongo.setValue(value, field.getType(), field);
                        }
                    }
                }

                vals.add(mongo);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return vals;
    }

    public void delete() {
        if (!this.getClass().isAnnotationPresent(CollectionName.class)) {
            Bukkit.getLogger().log(Level.SEVERE, "CollectionName Class not found while using MongoInitializer (" + this.getClass().getSimpleName() + ")");
            return;
        }

        String tableName = this.getClass().getAnnotation(CollectionName.class).name();
        MongoCollection<Document> col = Carbyne.getInstance().getMongoDatabase().getCollection(tableName);

        String identifier = null;
        Object identifierValue = null;

        for (Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            MongoColumn column = field.getAnnotation(MongoColumn.class);

            if (column != null) {
                if (column.identifier()) {
                    identifier = column.name();
                    identifierValue = getValue(field);
                }
            }
        }

        BasicDBObject searchQuery = new BasicDBObject().append(identifier, identifierValue);
        col.deleteOne(searchQuery);
    }

    @SuppressWarnings("unchecked")
    private boolean documentExists(BasicDBObject search, MongoCollection col) {
        FindIterable<Document> ret = (FindIterable<Document>) col.find(search).limit(1);
        return ret.first() != null;
    }

    @SuppressWarnings("unchecked")
    private String getValue(Field field) {
        try {
            Object o = field.get(this);

            if (o == null) {
                o = "NULL";
            }

            String ret = o.toString();

            if (!field.isAnnotationPresent(DatabaseSerializer.class))
                return ret;

            DatabaseSerializer serializer = field.getAnnotation(DatabaseSerializer.class);
            ret = serializer.serializer().newInstance().toString(o);

            return ret;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void setValue(Object value, Class<?> type, Field field) {
        try {
            if (type.isPrimitive()) {
                type = ClassUtils.primitiveToWrapper(type);
            }

            field.setAccessible(true);

            if (value == null || type.equals(value.getClass())) {
                field.set(this, value);
            } else if (field.isAnnotationPresent(DatabaseSerializer.class)) {
                AbstractSerializer serializer = field.getAnnotation(DatabaseSerializer.class).serializer().newInstance();
                field.set(this, serializer.fromString(value));
            } else if (type.equals(UUID.class)) {
                field.set(this, type.getDeclaredMethod("fromString", String.class).invoke(null, value.toString()));
            } else if (!Primitives.isWrapperType(type) && !type.equals(String.class) && !type.equals(Long.class) && !type.isPrimitive()) {
                field.set(this, type.getDeclaredMethod("valueOf", String.class).invoke(null, value.toString()));
            } else {
                field.set(this, type.getDeclaredMethod("valueOf", value.getClass()).invoke(null, value.toString()));
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}