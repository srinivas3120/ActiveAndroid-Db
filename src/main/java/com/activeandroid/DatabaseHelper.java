package com.activeandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.text.TextUtils;

import com.activeandroid.util.IOUtils;
import com.activeandroid.util.Log;
import com.activeandroid.util.NaturalOrderComparator;
import com.activeandroid.util.SQLiteUtils;
import com.activeandroid.util.SqlParser;

public final class DatabaseHelper extends SQLiteOpenHelper {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public final static String MIGRATION_PATH = "migrations";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE FIELDS
	//////////////////////////////////////////////////////////////////////////////////////

	private final Cache cache;
	private final String mSqlParser;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public DatabaseHelper(Cache cache, Configuration configuration) {
		super(configuration.getContext(), configuration.getDatabaseName(), null, configuration.getDatabaseVersion());
		copyAttachedDatabase(configuration.getContext(), configuration.getDatabaseName());
		mSqlParser = configuration.getSqlParser();
		this.cache = cache;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDEN METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onOpen(SQLiteDatabase db) {
		executePragmas(db);
	};

	@Override
	public void onCreate(SQLiteDatabase db) {
		executePragmas(db);
		executeCreate(db);
		try {
			Log.i("1. "+this.cache.getContext().getAssets().toString());
			Log.i("1. "+Arrays.toString(this.cache.getContext().getAssets().list(MIGRATION_PATH)));
		} catch (IOException e) {
			e.printStackTrace();
			Log.i("Cache.getContext().getAssets() exception");
		}
		executeMigrations(db, -1, db.getVersion());
		Log.i("after onCreate executeMigrations: getVersion: "+db.getVersion());
		executeCreateIndex(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		executePragmas(db);
		executeCreate(db);
		executeMigrations(db, oldVersion, newVersion);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void copyAttachedDatabase(Context context, String databaseName) {
		final File dbPath = context.getDatabasePath(databaseName);

		// If the database already exists, return
		if (dbPath.exists()) {
			return;
		}

		// Make sure we have a path to the file
		dbPath.getParentFile().mkdirs();

		// Try to copy database file
		try {
			final InputStream inputStream = context.getAssets().open(databaseName);
			final OutputStream output = new FileOutputStream(dbPath);

			byte[] buffer = new byte[8192];
			int length;

			while ((length = inputStream.read(buffer, 0, 8192)) > 0) {
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			inputStream.close();
		}
		catch (IOException e) {
			Log.e("Failed to open file", e);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void executePragmas(SQLiteDatabase db) {
		if (SQLiteUtils.FOREIGN_KEYS_SUPPORTED) {
			db.execSQL("PRAGMA foreign_keys=ON;");
			Log.i("Foreign Keys supported. Enabling foreign key features.");
		}
	}

	private void executeCreateIndex(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			for (TableInfo tableInfo : cache.getTableInfos()) {
				String[] definitions = SQLiteUtils.createIndexDefinition(tableInfo);

				for (String definition : definitions) {
					db.execSQL(definition);
				}
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	private void executeCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			for (TableInfo tableInfo : cache.getTableInfos()) {
				db.execSQL(SQLiteUtils.createTableDefinition(cache, tableInfo));
			}
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	private boolean executeMigrations(SQLiteDatabase db, int oldVersion, int newVersion) {
		boolean migrationExecuted = false;
		try {
			final List<String> files = Arrays.asList(cache.getContext().getAssets().list(MIGRATION_PATH));
			Collections.sort(files, new NaturalOrderComparator());

			db.beginTransaction();
			try {
				for (String file : files) {
					try {
						final int version = Integer.valueOf(file.replace(".sql", ""));

						if (version > oldVersion && version <= newVersion) {
							executeSqlScript(db, file);
							migrationExecuted = true;

							Log.i(file + " executed succesfully.");
						}
					}
					catch (NumberFormatException e) {
						Log.w("Skipping invalidly named file: " + file, e);
					}
				}
				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
			}
		}
		catch (IOException e) {
			Log.e("Failed to execute migrations.", e);
		}

		return migrationExecuted;
	}

	private void executeSqlScript(SQLiteDatabase db, String file) {

		InputStream stream = null;

		try {
			stream = cache.getContext().getAssets().open(MIGRATION_PATH + "/" + file);

			if (Configuration.SQL_PARSER_DELIMITED.equalsIgnoreCase(mSqlParser)) {
				executeDelimitedSqlScript(db, stream);

			} else {
				executeLegacySqlScript(db, stream);

			}

		} catch (IOException e) {
			Log.e("Failed to execute " + file, e);

		} finally {
			IOUtils.closeQuietly(stream);

		}
	}

	private void executeDelimitedSqlScript(SQLiteDatabase db, InputStream stream) throws IOException {

		List<String> commands = SqlParser.parse(stream);

		for(String command : commands) {
			db.execSQL(command);
		}
	}

	private void executeLegacySqlScript(SQLiteDatabase db, InputStream stream) throws IOException {

		InputStreamReader reader = null;
		BufferedReader buffer = null;

		try {
			reader = new InputStreamReader(stream);
			buffer = new BufferedReader(reader);
			String line = null;

			while ((line = buffer.readLine()) != null) {
				line = line.replace(";", "").trim();
				if (!TextUtils.isEmpty(line)) {
					db.execSQL(line);
				}
			}

		} finally {
			IOUtils.closeQuietly(buffer);
			IOUtils.closeQuietly(reader);

		}
	}
}
