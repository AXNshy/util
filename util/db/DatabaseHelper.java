package com.github.M.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by axnshy on 2017/6/27.
 */

public abstract class DatabaseHelper {


    private int DBVersion;

    private String DBName;

    private String[] mDBCreateSql;

    private String[] mDBUpgradeSql;

    private String[] mDBDeleteSql;

    private DBHelper mDBHelper;

    private SQLiteDatabase mDB;

    public abstract int getDBVersion();

    public abstract String getDBName();

    public abstract String[] getDBCreateSql();

    public abstract String[] getDBUpgradeSql();

    public abstract String[] getDBDeleteSql();

    public DatabaseHelper(Context context) {
        this.DBVersion = this.getDBVersion();
        this.DBName = this.getDBName();
        this.mDBCreateSql = this.getDBCreateSql();
        this.mDBUpgradeSql = this.getDBUpgradeSql();
        this.mDBDeleteSql = this.getDBDeleteSql();
        this.mDBHelper = new DBHelper(context, this.DBName, null, this.DBVersion);
    }


    protected void open() {
        mDB = mDBHelper.getWritableDatabase();
    }

    protected void close() {
        mDB.close();
        mDBHelper.close();
    }

    /*
    *
    * database 增删改查操作
    *
    * */

    private void contentValuesPut(ContentValues values, String key, Object value) {

        if (value == null) {
            values.put(key, "");
        } else {
            String classname = value.getClass().getName();
            if ("java.lang.String".equals(classname)) {
                values.put(key, String.valueOf(value.toString()));
            } else if ("java.lang.Integer".equals(classname)) {
                values.put(key, Integer.valueOf(value.toString()));
            } else if ("java.lang.Short".equals(classname)) {
                values.put(key, Short.valueOf(value.toString()));
            } else if ("java.lang.Float".equals(classname)) {
                values.put(key, Float.valueOf(value.toString()));
            } else if ("java.lang.Double".equals(classname)) {
                values.put(key, Double.valueOf(value.toString()));
            } else if ("java.lang.Boolean".equals(classname)) {
                values.put(key, Boolean.valueOf(value.toString()));
            } else if ("java.lang.Byte".equals(classname)) {
                values.put(key, Byte.valueOf(value.toString()));
            } else if ("java.lang.Long".equals(classname)) {
                values.put(key, Long.valueOf(value.toString()));
            }
        }
    }

    private String convertWhereSqlFromArray(String[] whereColumns) {
        StringBuilder builder = new StringBuilder();
        builder.append("WHERE ");
        for (int i = 0; i < whereColumns.length; i++) {
            builder.append(whereColumns[i] + " = ? ");
            if (i != whereColumns.length - 1) {
                builder.append(" AND ");
            }
        }
        return builder.toString();
    }


    /**
     * insert
     *
     * @param tableName
     * @param keys
     * @param values
     * @return
     */
    public boolean insert(String tableName, String[] keys, Object[] values) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < keys.length; i++) {
            contentValuesPut(contentValues, keys[i], values[i]);
        }
        long rowId = mDB.insert(tableName, null, contentValues);
        return rowId != -1;
    }

    /**
     * insert
     *
     * @param tableName
     * @param map
     * @return
     */
    public boolean insert(String tableName, Map<String, Object> map) {
        ContentValues contentValues = new ContentValues();
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            contentValuesPut(contentValues, key, map.get(key));
        }
        long rowId = mDB.insert(tableName, null, contentValues);
        return rowId != -1;
    }


    /**
     * update
     *
     * @param tableName
     * @param keys
     * @param values
     * @param whereColumns
     * @param whereArgs
     * @return
     */
    public boolean update(String tableName, String[] keys, Object[] values, String[] whereColumns, String[] whereArgs) {
        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < keys.length; i++) {
            contentValuesPut(contentValues, keys[i], values[i]);
        }
        int rowNum = mDB.update(tableName, contentValues, convertWhereSqlFromArray(whereColumns), whereArgs);
        return rowNum > 0;
    }


    /**
     * @param tableName
     * @param map
     * @param whereParams
     * @return
     */
    public boolean update(String tableName, Map<String, Object> map, Map<String, String> whereParams) {
        ContentValues contentValues = new ContentValues();
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            contentValuesPut(contentValues, key, map.get(key));
        }
        iterator.remove();
        String[] whereColumns = (String[]) whereParams.keySet().toArray();
        String[] whereArgs = (String[]) whereParams.values().toArray();
        int rowNum = mDB.update(tableName, contentValues, convertWhereSqlFromArray(whereColumns), whereArgs);
        return rowNum > 0;
    }


    /**
     * delete
     *
     * @param table
     * @param whereColumns
     * @param whereArgs
     * @return
     */
    public boolean delete(String table, String[] whereColumns, String[] whereArgs) {
        int rowNum = mDB.delete(table, convertWhereSqlFromArray(whereColumns), whereArgs);
        return rowNum > 0;
    }

    /**
     * delete
     *
     * @param table
     * @param whereParams
     * @return
     */
    public boolean delete(String table, Map<String, String> whereParams) {
        int rowNum = mDB.delete(table
                , convertWhereSqlFromArray((String[]) whereParams.keySet().toArray())
                , (String[]) whereParams.values().toArray());
        return rowNum > 0;
    }

    /**
     * @param sql
     * @param selectArgs
     * @return
     */
    public List<Map> query(String sql, String[] selectArgs) {
        List<Map> list = new ArrayList<>();
        Cursor cursor = mDB.rawQuery(sql, selectArgs);
        int columnCount = cursor.getColumnCount();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, Object> map = new HashMap<>();
                for (int i = 0; i < columnCount; i++) {
                    int type = cursor.getType(i);
                    switch (type) {
                        case 0:
                            break;
                        case 1:
                            map.put(cursor.getColumnName(i), cursor.getInt(cursor.getColumnIndexOrThrow(selectArgs[i])));
                            break;
                        case 2:
                            map.put(cursor.getColumnName(i), cursor.getFloat(cursor.getColumnIndexOrThrow(selectArgs[i])));
                            break;
                        case 3:
                            map.put(cursor.getColumnName(i), cursor.getString(cursor.getColumnIndexOrThrow(selectArgs[i])));
                            break;
                        case 4:
                            map.put(cursor.getColumnName(i), cursor.getBlob(cursor.getColumnIndexOrThrow(selectArgs[i])));

                    }
                }
                list.add(map);
            } while (cursor.moveToLast());
        }
        cursor.close();
        cursor = null;
        return list;
    }

    public void execSQL(String sql) {
        this.mDB.execSQL(sql);
    }

    public void execSQL(String sql, Object[] params) {
        this.mDB.execSQL(sql, params);
    }


    private class DBHelper extends SQLiteOpenHelper {


        public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            String[] arr = DatabaseHelper.this.mDBCreateSql;
            for (String sql : arr)
                db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String[] arr = DatabaseHelper.this.mDBUpgradeSql;
            for (String sql : arr)
                db.execSQL(sql);

        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                String[] arr = DatabaseHelper.this.mDBDeleteSql;
                for (String sql : arr)
                    db.execSQL(sql);
            }
        }
    }
}
