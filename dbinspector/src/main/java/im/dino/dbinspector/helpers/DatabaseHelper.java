package im.dino.dbinspector.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dino on 23/02/14.
 */
public class DatabaseHelper {

    public static final String LOGTAG = "DBINSPECTOR";

    public static final String COLUMN_CID = "cid";

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_TYPE = "type";

    public static final String COLUMN_NOT_NULL = "not null";

    public static final String COLUMN_DEFAULT = "default value";

    public static final String COLUMN_PRIMARY = "primary key";

    public static final String TABLE_LIST_QUERY
            = "SELECT name FROM sqlite_master WHERE type='table'";

    public static final String PRAGMA_FORMAT = "PRAGMA table_info(%s)";

    public static String getSqliteDir(Context context) {
        return context.getFilesDir().getParent() + File.separator + "databases" + File.separator;
    }

    public static List<File> getDatabaseList(Context context) {
        List<File> databaseList = new ArrayList<>();

        File sqliteDir = new File(DatabaseHelper.getSqliteDir(context));

        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.endsWith(".sql")
                        || filename.endsWith(".sqlite")
                        || filename.endsWith(".db")
                        || filename.endsWith(".cblite");
            }
        };

        // look for standard sqlite databases in the databases dir
        File[] sqliteFiles = sqliteDir.listFiles(filenameFilter);
        if (sqliteFiles != null) {
            databaseList.addAll(Arrays.asList(sqliteFiles));
        } else {
            Log.d(LOGTAG, "Database file list is null!");
        }

        // CouchBase Lite stores the databases in the app files dir
        String[] cbliteFiles = context.fileList();
        for (String filename : cbliteFiles) {
            if (filenameFilter.accept(context.getFilesDir(), filename)) {
                databaseList.add(new File(context.getFilesDir(), filename));
            }
        }

        return databaseList;
    }

    public static List<String> getAllTables(File database) {

        CursorOperation<List<String>> operation = new CursorOperation<List<String>>(database) {
            @Override
            public Cursor provideCursor(SQLiteDatabase database) {
                return database.rawQuery(TABLE_LIST_QUERY, null);
            }

            @Override
            public List<String> provideResult(SQLiteDatabase database, Cursor cursor) {
                List<String> tableList = new ArrayList<>();
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        tableList.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                        cursor.moveToNext();
                    }
                }
                return tableList;
            }
        };

        return operation.execute();
    }

}
