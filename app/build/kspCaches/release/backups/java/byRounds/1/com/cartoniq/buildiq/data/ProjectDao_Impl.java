package com.cartoniq.buildiq.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProjectDao_Impl implements ProjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProjectEntity> __insertionAdapterOfProjectEntity;

  private final EntityInsertionAdapter<PriceEntity> __insertionAdapterOfPriceEntity;

  private final EntityDeletionOrUpdateAdapter<ProjectEntity> __deletionAdapterOfProjectEntity;

  private final EntityDeletionOrUpdateAdapter<ProjectEntity> __updateAdapterOfProjectEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteProjectById;

  private final SharedSQLiteStatement __preparedStmtOfClearProjects;

  private final SharedSQLiteStatement __preparedStmtOfDeletePrice;

  private final SharedSQLiteStatement __preparedStmtOfClearPrices;

  public ProjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProjectEntity = new EntityInsertionAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `projects` (`id`,`title`,`summary`,`totalCost`,`createdAt`,`clientName`,`clientAddress`,`clientPhone`,`clientEmail`,`notes`,`laborType`,`laborRate`,`laborHours`,`laborCost`,`taxRate`,`taxAmount`,`contingencyRate`,`contingencyAmount`,`markupRate`,`markupAmount`,`currencySymbol`,`itemsJson`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProjectEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getSummary());
        statement.bindDouble(4, entity.getTotalCost());
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindString(6, entity.getClientName());
        statement.bindString(7, entity.getClientAddress());
        statement.bindString(8, entity.getClientPhone());
        statement.bindString(9, entity.getClientEmail());
        statement.bindString(10, entity.getNotes());
        statement.bindString(11, entity.getLaborType());
        statement.bindDouble(12, entity.getLaborRate());
        statement.bindDouble(13, entity.getLaborHours());
        statement.bindDouble(14, entity.getLaborCost());
        statement.bindDouble(15, entity.getTaxRate());
        statement.bindDouble(16, entity.getTaxAmount());
        statement.bindDouble(17, entity.getContingencyRate());
        statement.bindDouble(18, entity.getContingencyAmount());
        statement.bindDouble(19, entity.getMarkupRate());
        statement.bindDouble(20, entity.getMarkupAmount());
        statement.bindString(21, entity.getCurrencySymbol());
        statement.bindString(22, entity.getItemsJson());
      }
    };
    this.__insertionAdapterOfPriceEntity = new EntityInsertionAdapter<PriceEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `prices` (`key`,`value`,`label`,`unit`,`category`,`isCustom`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PriceEntity entity) {
        statement.bindString(1, entity.getKey());
        statement.bindDouble(2, entity.getValue());
        statement.bindString(3, entity.getLabel());
        statement.bindString(4, entity.getUnit());
        statement.bindString(5, entity.getCategory());
        final int _tmp = entity.isCustom() ? 1 : 0;
        statement.bindLong(6, _tmp);
      }
    };
    this.__deletionAdapterOfProjectEntity = new EntityDeletionOrUpdateAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `projects` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProjectEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfProjectEntity = new EntityDeletionOrUpdateAdapter<ProjectEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `projects` SET `id` = ?,`title` = ?,`summary` = ?,`totalCost` = ?,`createdAt` = ?,`clientName` = ?,`clientAddress` = ?,`clientPhone` = ?,`clientEmail` = ?,`notes` = ?,`laborType` = ?,`laborRate` = ?,`laborHours` = ?,`laborCost` = ?,`taxRate` = ?,`taxAmount` = ?,`contingencyRate` = ?,`contingencyAmount` = ?,`markupRate` = ?,`markupAmount` = ?,`currencySymbol` = ?,`itemsJson` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProjectEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getSummary());
        statement.bindDouble(4, entity.getTotalCost());
        statement.bindLong(5, entity.getCreatedAt());
        statement.bindString(6, entity.getClientName());
        statement.bindString(7, entity.getClientAddress());
        statement.bindString(8, entity.getClientPhone());
        statement.bindString(9, entity.getClientEmail());
        statement.bindString(10, entity.getNotes());
        statement.bindString(11, entity.getLaborType());
        statement.bindDouble(12, entity.getLaborRate());
        statement.bindDouble(13, entity.getLaborHours());
        statement.bindDouble(14, entity.getLaborCost());
        statement.bindDouble(15, entity.getTaxRate());
        statement.bindDouble(16, entity.getTaxAmount());
        statement.bindDouble(17, entity.getContingencyRate());
        statement.bindDouble(18, entity.getContingencyAmount());
        statement.bindDouble(19, entity.getMarkupRate());
        statement.bindDouble(20, entity.getMarkupAmount());
        statement.bindString(21, entity.getCurrencySymbol());
        statement.bindString(22, entity.getItemsJson());
        statement.bindLong(23, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteProjectById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM projects WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearProjects = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM projects";
        return _query;
      }
    };
    this.__preparedStmtOfDeletePrice = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM prices WHERE `key` = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearPrices = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM prices";
        return _query;
      }
    };
  }

  @Override
  public Object insertProject(final ProjectEntity project,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfProjectEntity.insertAndReturnId(project);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object upsertPrice(final PriceEntity price, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPriceEntity.insert(price);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProject(final ProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfProjectEntity.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateProject(final ProjectEntity project,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfProjectEntity.handle(project);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteProjectById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteProjectById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteProjectById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearProjects(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearProjects.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearProjects.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deletePrice(final String key, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeletePrice.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, key);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeletePrice.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPrices(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPrices.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearPrices.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public LiveData<List<ProjectEntity>> observeProjects() {
    final String _sql = "SELECT * FROM projects ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"projects"}, false, new Callable<List<ProjectEntity>>() {
      @Override
      @Nullable
      public List<ProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "clientAddress");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLaborType = CursorUtil.getColumnIndexOrThrow(_cursor, "laborType");
          final int _cursorIndexOfLaborRate = CursorUtil.getColumnIndexOrThrow(_cursor, "laborRate");
          final int _cursorIndexOfLaborHours = CursorUtil.getColumnIndexOrThrow(_cursor, "laborHours");
          final int _cursorIndexOfLaborCost = CursorUtil.getColumnIndexOrThrow(_cursor, "laborCost");
          final int _cursorIndexOfTaxRate = CursorUtil.getColumnIndexOrThrow(_cursor, "taxRate");
          final int _cursorIndexOfTaxAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "taxAmount");
          final int _cursorIndexOfContingencyRate = CursorUtil.getColumnIndexOrThrow(_cursor, "contingencyRate");
          final int _cursorIndexOfContingencyAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "contingencyAmount");
          final int _cursorIndexOfMarkupRate = CursorUtil.getColumnIndexOrThrow(_cursor, "markupRate");
          final int _cursorIndexOfMarkupAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "markupAmount");
          final int _cursorIndexOfCurrencySymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "currencySymbol");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "itemsJson");
          final List<ProjectEntity> _result = new ArrayList<ProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProjectEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpClientName;
            _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            final String _tmpClientAddress;
            _tmpClientAddress = _cursor.getString(_cursorIndexOfClientAddress);
            final String _tmpClientPhone;
            _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            final String _tmpClientEmail;
            _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLaborType;
            _tmpLaborType = _cursor.getString(_cursorIndexOfLaborType);
            final double _tmpLaborRate;
            _tmpLaborRate = _cursor.getDouble(_cursorIndexOfLaborRate);
            final double _tmpLaborHours;
            _tmpLaborHours = _cursor.getDouble(_cursorIndexOfLaborHours);
            final double _tmpLaborCost;
            _tmpLaborCost = _cursor.getDouble(_cursorIndexOfLaborCost);
            final double _tmpTaxRate;
            _tmpTaxRate = _cursor.getDouble(_cursorIndexOfTaxRate);
            final double _tmpTaxAmount;
            _tmpTaxAmount = _cursor.getDouble(_cursorIndexOfTaxAmount);
            final double _tmpContingencyRate;
            _tmpContingencyRate = _cursor.getDouble(_cursorIndexOfContingencyRate);
            final double _tmpContingencyAmount;
            _tmpContingencyAmount = _cursor.getDouble(_cursorIndexOfContingencyAmount);
            final double _tmpMarkupRate;
            _tmpMarkupRate = _cursor.getDouble(_cursorIndexOfMarkupRate);
            final double _tmpMarkupAmount;
            _tmpMarkupAmount = _cursor.getDouble(_cursorIndexOfMarkupAmount);
            final String _tmpCurrencySymbol;
            _tmpCurrencySymbol = _cursor.getString(_cursorIndexOfCurrencySymbol);
            final String _tmpItemsJson;
            _tmpItemsJson = _cursor.getString(_cursorIndexOfItemsJson);
            _item = new ProjectEntity(_tmpId,_tmpTitle,_tmpSummary,_tmpTotalCost,_tmpCreatedAt,_tmpClientName,_tmpClientAddress,_tmpClientPhone,_tmpClientEmail,_tmpNotes,_tmpLaborType,_tmpLaborRate,_tmpLaborHours,_tmpLaborCost,_tmpTaxRate,_tmpTaxAmount,_tmpContingencyRate,_tmpContingencyAmount,_tmpMarkupRate,_tmpMarkupAmount,_tmpCurrencySymbol,_tmpItemsJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllProjects(final Continuation<? super List<ProjectEntity>> $completion) {
    final String _sql = "SELECT * FROM projects ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProjectEntity>>() {
      @Override
      @NonNull
      public List<ProjectEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "clientAddress");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLaborType = CursorUtil.getColumnIndexOrThrow(_cursor, "laborType");
          final int _cursorIndexOfLaborRate = CursorUtil.getColumnIndexOrThrow(_cursor, "laborRate");
          final int _cursorIndexOfLaborHours = CursorUtil.getColumnIndexOrThrow(_cursor, "laborHours");
          final int _cursorIndexOfLaborCost = CursorUtil.getColumnIndexOrThrow(_cursor, "laborCost");
          final int _cursorIndexOfTaxRate = CursorUtil.getColumnIndexOrThrow(_cursor, "taxRate");
          final int _cursorIndexOfTaxAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "taxAmount");
          final int _cursorIndexOfContingencyRate = CursorUtil.getColumnIndexOrThrow(_cursor, "contingencyRate");
          final int _cursorIndexOfContingencyAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "contingencyAmount");
          final int _cursorIndexOfMarkupRate = CursorUtil.getColumnIndexOrThrow(_cursor, "markupRate");
          final int _cursorIndexOfMarkupAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "markupAmount");
          final int _cursorIndexOfCurrencySymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "currencySymbol");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "itemsJson");
          final List<ProjectEntity> _result = new ArrayList<ProjectEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProjectEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpClientName;
            _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            final String _tmpClientAddress;
            _tmpClientAddress = _cursor.getString(_cursorIndexOfClientAddress);
            final String _tmpClientPhone;
            _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            final String _tmpClientEmail;
            _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLaborType;
            _tmpLaborType = _cursor.getString(_cursorIndexOfLaborType);
            final double _tmpLaborRate;
            _tmpLaborRate = _cursor.getDouble(_cursorIndexOfLaborRate);
            final double _tmpLaborHours;
            _tmpLaborHours = _cursor.getDouble(_cursorIndexOfLaborHours);
            final double _tmpLaborCost;
            _tmpLaborCost = _cursor.getDouble(_cursorIndexOfLaborCost);
            final double _tmpTaxRate;
            _tmpTaxRate = _cursor.getDouble(_cursorIndexOfTaxRate);
            final double _tmpTaxAmount;
            _tmpTaxAmount = _cursor.getDouble(_cursorIndexOfTaxAmount);
            final double _tmpContingencyRate;
            _tmpContingencyRate = _cursor.getDouble(_cursorIndexOfContingencyRate);
            final double _tmpContingencyAmount;
            _tmpContingencyAmount = _cursor.getDouble(_cursorIndexOfContingencyAmount);
            final double _tmpMarkupRate;
            _tmpMarkupRate = _cursor.getDouble(_cursorIndexOfMarkupRate);
            final double _tmpMarkupAmount;
            _tmpMarkupAmount = _cursor.getDouble(_cursorIndexOfMarkupAmount);
            final String _tmpCurrencySymbol;
            _tmpCurrencySymbol = _cursor.getString(_cursorIndexOfCurrencySymbol);
            final String _tmpItemsJson;
            _tmpItemsJson = _cursor.getString(_cursorIndexOfItemsJson);
            _item = new ProjectEntity(_tmpId,_tmpTitle,_tmpSummary,_tmpTotalCost,_tmpCreatedAt,_tmpClientName,_tmpClientAddress,_tmpClientPhone,_tmpClientEmail,_tmpNotes,_tmpLaborType,_tmpLaborRate,_tmpLaborHours,_tmpLaborCost,_tmpTaxRate,_tmpTaxAmount,_tmpContingencyRate,_tmpContingencyAmount,_tmpMarkupRate,_tmpMarkupAmount,_tmpCurrencySymbol,_tmpItemsJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getProjectById(final long id,
      final Continuation<? super ProjectEntity> $completion) {
    final String _sql = "SELECT * FROM projects WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ProjectEntity>() {
      @Override
      @Nullable
      public ProjectEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSummary = CursorUtil.getColumnIndexOrThrow(_cursor, "summary");
          final int _cursorIndexOfTotalCost = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCost");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfClientName = CursorUtil.getColumnIndexOrThrow(_cursor, "clientName");
          final int _cursorIndexOfClientAddress = CursorUtil.getColumnIndexOrThrow(_cursor, "clientAddress");
          final int _cursorIndexOfClientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "clientPhone");
          final int _cursorIndexOfClientEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "clientEmail");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLaborType = CursorUtil.getColumnIndexOrThrow(_cursor, "laborType");
          final int _cursorIndexOfLaborRate = CursorUtil.getColumnIndexOrThrow(_cursor, "laborRate");
          final int _cursorIndexOfLaborHours = CursorUtil.getColumnIndexOrThrow(_cursor, "laborHours");
          final int _cursorIndexOfLaborCost = CursorUtil.getColumnIndexOrThrow(_cursor, "laborCost");
          final int _cursorIndexOfTaxRate = CursorUtil.getColumnIndexOrThrow(_cursor, "taxRate");
          final int _cursorIndexOfTaxAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "taxAmount");
          final int _cursorIndexOfContingencyRate = CursorUtil.getColumnIndexOrThrow(_cursor, "contingencyRate");
          final int _cursorIndexOfContingencyAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "contingencyAmount");
          final int _cursorIndexOfMarkupRate = CursorUtil.getColumnIndexOrThrow(_cursor, "markupRate");
          final int _cursorIndexOfMarkupAmount = CursorUtil.getColumnIndexOrThrow(_cursor, "markupAmount");
          final int _cursorIndexOfCurrencySymbol = CursorUtil.getColumnIndexOrThrow(_cursor, "currencySymbol");
          final int _cursorIndexOfItemsJson = CursorUtil.getColumnIndexOrThrow(_cursor, "itemsJson");
          final ProjectEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpSummary;
            _tmpSummary = _cursor.getString(_cursorIndexOfSummary);
            final double _tmpTotalCost;
            _tmpTotalCost = _cursor.getDouble(_cursorIndexOfTotalCost);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpClientName;
            _tmpClientName = _cursor.getString(_cursorIndexOfClientName);
            final String _tmpClientAddress;
            _tmpClientAddress = _cursor.getString(_cursorIndexOfClientAddress);
            final String _tmpClientPhone;
            _tmpClientPhone = _cursor.getString(_cursorIndexOfClientPhone);
            final String _tmpClientEmail;
            _tmpClientEmail = _cursor.getString(_cursorIndexOfClientEmail);
            final String _tmpNotes;
            _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            final String _tmpLaborType;
            _tmpLaborType = _cursor.getString(_cursorIndexOfLaborType);
            final double _tmpLaborRate;
            _tmpLaborRate = _cursor.getDouble(_cursorIndexOfLaborRate);
            final double _tmpLaborHours;
            _tmpLaborHours = _cursor.getDouble(_cursorIndexOfLaborHours);
            final double _tmpLaborCost;
            _tmpLaborCost = _cursor.getDouble(_cursorIndexOfLaborCost);
            final double _tmpTaxRate;
            _tmpTaxRate = _cursor.getDouble(_cursorIndexOfTaxRate);
            final double _tmpTaxAmount;
            _tmpTaxAmount = _cursor.getDouble(_cursorIndexOfTaxAmount);
            final double _tmpContingencyRate;
            _tmpContingencyRate = _cursor.getDouble(_cursorIndexOfContingencyRate);
            final double _tmpContingencyAmount;
            _tmpContingencyAmount = _cursor.getDouble(_cursorIndexOfContingencyAmount);
            final double _tmpMarkupRate;
            _tmpMarkupRate = _cursor.getDouble(_cursorIndexOfMarkupRate);
            final double _tmpMarkupAmount;
            _tmpMarkupAmount = _cursor.getDouble(_cursorIndexOfMarkupAmount);
            final String _tmpCurrencySymbol;
            _tmpCurrencySymbol = _cursor.getString(_cursorIndexOfCurrencySymbol);
            final String _tmpItemsJson;
            _tmpItemsJson = _cursor.getString(_cursorIndexOfItemsJson);
            _result = new ProjectEntity(_tmpId,_tmpTitle,_tmpSummary,_tmpTotalCost,_tmpCreatedAt,_tmpClientName,_tmpClientAddress,_tmpClientPhone,_tmpClientEmail,_tmpNotes,_tmpLaborType,_tmpLaborRate,_tmpLaborHours,_tmpLaborCost,_tmpTaxRate,_tmpTaxAmount,_tmpContingencyRate,_tmpContingencyAmount,_tmpMarkupRate,_tmpMarkupAmount,_tmpCurrencySymbol,_tmpItemsJson);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPrices(final Continuation<? super List<PriceEntity>> $completion) {
    final String _sql = "SELECT * FROM prices";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PriceEntity>>() {
      @Override
      @NonNull
      public List<PriceEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfKey = CursorUtil.getColumnIndexOrThrow(_cursor, "key");
          final int _cursorIndexOfValue = CursorUtil.getColumnIndexOrThrow(_cursor, "value");
          final int _cursorIndexOfLabel = CursorUtil.getColumnIndexOrThrow(_cursor, "label");
          final int _cursorIndexOfUnit = CursorUtil.getColumnIndexOrThrow(_cursor, "unit");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final List<PriceEntity> _result = new ArrayList<PriceEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PriceEntity _item;
            final String _tmpKey;
            _tmpKey = _cursor.getString(_cursorIndexOfKey);
            final double _tmpValue;
            _tmpValue = _cursor.getDouble(_cursorIndexOfValue);
            final String _tmpLabel;
            _tmpLabel = _cursor.getString(_cursorIndexOfLabel);
            final String _tmpUnit;
            _tmpUnit = _cursor.getString(_cursorIndexOfUnit);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final boolean _tmpIsCustom;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp != 0;
            _item = new PriceEntity(_tmpKey,_tmpValue,_tmpLabel,_tmpUnit,_tmpCategory,_tmpIsCustom);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
