package com.soso.evaextra;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class SimpleDb {

	private Context mContext;

	private Db mDb;

	public void close() {
		if (mDb != null) {
			mDb.close();
		}
	}

	public SimpleDb(Context context) {
		mContext = context;
		mDb = new Db(mContext);
	}

	public long insert(ContentValues values) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		return db.insert(Db.NAME, null, values);
	}

	public int update(ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		return db.update(Db.NAME, values, where, whereArgs);
	}

	public Cursor query(String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDb.getReadableDatabase();
		SQLiteQueryBuilder sqb = new SQLiteQueryBuilder();
		sqb.setTables(Db.NAME);
		Cursor c = sqb.query(db, null, selection, selectionArgs, null, null,
				"log_start DESC");
		return c;
	}

	public int delete(long id) {
		SQLiteDatabase db = mDb.getWritableDatabase();
		return db
				.delete(Db.NAME, "_id = ?", new String[] { Long.toString(id) });
	}

	public static class Db extends SQLiteOpenHelper {

		static final String NAME = "logs";
		private static final int VERSION = 1;

		public Db(Context context) {
			super(context, NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table "
					+ NAME
					+ " (_id integer primary key, log_name text, log_start integer, log_duration integer, uploaded integer, log_path text)");
			System.out.println("table created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO
			db.execSQL("DROP TABLE IF EXISTS " + NAME);
		}

	}

	public static class LogEntry implements BaseColumns {
		public static final String COLUMN_NAME_LOGNAME = "log_name";
		public static final String COLUMN_NAME_LOGSTART = "log_start";
		public static final String COLUMN_NAME_LOGDURATION = "log_duration";
		public static final String COLUMN_NAME_UPLOADED = "uploaded";
		public static final String COLUMN_NAME_LOGPATH = "log_path";

		long _id;
		String log_name;
		long log_start;
		long log_duration;
		boolean uploaded;
		String log_path;

		public LogEntry setLogName(String name) {
			log_name = name;
			return this;
		}

		public LogEntry setLogStart(long start) {
			log_start = start;
			return this;
		}
	}

	public static class SimpleDbUtil {
		public static long insert(SimpleDb db, LogEntry entry) {
			ContentValues values = new ContentValues();
			values.put(LogEntry.COLUMN_NAME_LOGNAME, entry.log_name);
			values.put(LogEntry.COLUMN_NAME_LOGSTART, entry.log_start);
			values.put(LogEntry.COLUMN_NAME_LOGDURATION, entry.log_duration);
			values.put(LogEntry.COLUMN_NAME_UPLOADED, entry.uploaded ? 1 : 0);
			values.put(LogEntry.COLUMN_NAME_LOGPATH, entry.log_path);
			return db.insert(values);
		}

		public static LogEntry findById(SimpleDb db, long id) {
			LogEntry entry = new LogEntry();
			Cursor cursor = db.query("_id = ?",
					new String[] { Long.toString(id) });
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					to(cursor, entry);
				}
				cursor.close();
			}
			return entry;
		}

		private static LogEntry to(Cursor cursor, LogEntry entry) {
			entry._id = cursor.getLong(0);
			entry.log_name = cursor.getString(1);
			entry.log_start = cursor.getLong(2);
			entry.log_duration = cursor.getLong(3);
			entry.uploaded = cursor.getInt(4) == 0 ? false : true;
			entry.log_path = cursor.getString(5);
			return entry;
		}

		public static int update(SimpleDb db, LogEntry entry) {
			ContentValues values = new ContentValues();
			values.put(LogEntry._ID, entry._id);
			values.put(LogEntry.COLUMN_NAME_LOGNAME, entry.log_name);
			values.put(LogEntry.COLUMN_NAME_LOGSTART, entry.log_start);
			values.put(LogEntry.COLUMN_NAME_LOGDURATION, entry.log_duration);
			values.put(LogEntry.COLUMN_NAME_UPLOADED, entry.uploaded ? 1 : 0);
			values.put(LogEntry.COLUMN_NAME_LOGPATH, entry.log_path);
			return db.update(values, "_id = ?",
					new String[] { Long.toString(entry._id) });
		}

		public static List<LogEntry> findAll(SimpleDb db) {
			return findAll(db, null, null);
		}

		public static List<LogEntry> findAll(SimpleDb db, String selection,
				String[] selectionArgs) {
			Cursor cursor = db.query(selection, selectionArgs);
			List<LogEntry> entries = new ArrayList<SimpleDb.LogEntry>();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						LogEntry entry = new LogEntry();
						entries.add(to(cursor, entry));

						cursor.moveToNext();
					}
				}
				cursor.close();
			}
			return entries;
		}

		public static List<LogEntry> findUploaded(SimpleDb db) {
			return findAll(db, "uploaded = ?", new String[] { "1" });
		}

		public static List<LogEntry> findUnuploaded(SimpleDb db) {
			return findAll(db, "uploaded = ?", new String[] { "0" });
		}

		public static List<LogEntry> findToday(SimpleDb db) {
			Calendar c = GregorianCalendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			return findAll(db, "log_start > ?",
					new String[] { Long.toString(c.getTimeInMillis()) });
		}

		public static List<LogEntry> findYesterday(SimpleDb db) {
			Calendar c = GregorianCalendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);
			long uppper = c.getTimeInMillis();
			c.add(Calendar.DATE, -1);
			long lower = c.getTimeInMillis();
			return findAll(db, "log_start > ? and log_start < ?", new String[] {
					Long.toString(lower), Long.toString(uppper) });
		}
	}
}
