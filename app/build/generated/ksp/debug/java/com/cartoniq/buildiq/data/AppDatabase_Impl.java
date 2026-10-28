package com.cartoniq.buildiq.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ProjectDao _projectDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `projects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `summary` TEXT NOT NULL, `totalCost` REAL NOT NULL, `createdAt` INTEGER NOT NULL, `clientName` TEXT NOT NULL, `clientAddress` TEXT NOT NULL, `clientPhone` TEXT NOT NULL, `clientEmail` TEXT NOT NULL, `notes` TEXT NOT NULL, `laborType` TEXT NOT NULL, `laborRate` REAL NOT NULL, `laborHours` REAL NOT NULL, `laborCost` REAL NOT NULL, `taxRate` REAL NOT NULL, `taxAmount` REAL NOT NULL, `contingencyRate` REAL NOT NULL, `contingencyAmount` REAL NOT NULL, `markupRate` REAL NOT NULL, `markupAmount` REAL NOT NULL, `currencySymbol` TEXT NOT NULL, `itemsJson` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `prices` (`key` TEXT NOT NULL, `value` REAL NOT NULL, `label` TEXT NOT NULL, `unit` TEXT NOT NULL, `category` TEXT NOT NULL, `isCustom` INTEGER NOT NULL, PRIMARY KEY(`key`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '0385e0e7f03925ae7baef7ad90ff6289')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `projects`");
        db.execSQL("DROP TABLE IF EXISTS `prices`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsProjects = new HashMap<String, TableInfo.Column>(22);
        _columnsProjects.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("summary", new TableInfo.Column("summary", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("totalCost", new TableInfo.Column("totalCost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("clientName", new TableInfo.Column("clientName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("clientAddress", new TableInfo.Column("clientAddress", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("clientPhone", new TableInfo.Column("clientPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("clientEmail", new TableInfo.Column("clientEmail", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("notes", new TableInfo.Column("notes", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("laborType", new TableInfo.Column("laborType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("laborRate", new TableInfo.Column("laborRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("laborHours", new TableInfo.Column("laborHours", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("laborCost", new TableInfo.Column("laborCost", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("taxRate", new TableInfo.Column("taxRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("taxAmount", new TableInfo.Column("taxAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("contingencyRate", new TableInfo.Column("contingencyRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("contingencyAmount", new TableInfo.Column("contingencyAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("markupRate", new TableInfo.Column("markupRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("markupAmount", new TableInfo.Column("markupAmount", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("currencySymbol", new TableInfo.Column("currencySymbol", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProjects.put("itemsJson", new TableInfo.Column("itemsJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProjects = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProjects = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProjects = new TableInfo("projects", _columnsProjects, _foreignKeysProjects, _indicesProjects);
        final TableInfo _existingProjects = TableInfo.read(db, "projects");
        if (!_infoProjects.equals(_existingProjects)) {
          return new RoomOpenHelper.ValidationResult(false, "projects(com.cartoniq.buildiq.data.ProjectEntity).\n"
                  + " Expected:\n" + _infoProjects + "\n"
                  + " Found:\n" + _existingProjects);
        }
        final HashMap<String, TableInfo.Column> _columnsPrices = new HashMap<String, TableInfo.Column>(6);
        _columnsPrices.put("key", new TableInfo.Column("key", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPrices.put("value", new TableInfo.Column("value", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPrices.put("label", new TableInfo.Column("label", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPrices.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPrices.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPrices.put("isCustom", new TableInfo.Column("isCustom", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPrices = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPrices = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPrices = new TableInfo("prices", _columnsPrices, _foreignKeysPrices, _indicesPrices);
        final TableInfo _existingPrices = TableInfo.read(db, "prices");
        if (!_infoPrices.equals(_existingPrices)) {
          return new RoomOpenHelper.ValidationResult(false, "prices(com.cartoniq.buildiq.data.PriceEntity).\n"
                  + " Expected:\n" + _infoPrices + "\n"
                  + " Found:\n" + _existingPrices);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "0385e0e7f03925ae7baef7ad90ff6289", "7c9eba7688aa2941a8bcc4688821554e");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "projects","prices");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `projects`");
      _db.execSQL("DELETE FROM `prices`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ProjectDao.class, ProjectDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ProjectDao projectDao() {
    if (_projectDao != null) {
      return _projectDao;
    } else {
      synchronized(this) {
        if(_projectDao == null) {
          _projectDao = new ProjectDao_Impl(this);
        }
        return _projectDao;
      }
    }
  }
}
