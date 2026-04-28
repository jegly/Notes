package com.kin.easynotes.`data`.local.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.kin.easynotes.`data`.local.dao.NoteDao
import com.kin.easynotes.`data`.local.dao.NoteDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class NoteDatabase_Impl : NoteDatabase() {
  private val _noteDao: Lazy<NoteDao> = lazy {
    NoteDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(4,
        "3b789c318a699e5ce7dfb3e9fbdf0053", "47a9f33ba16226c955680e72daa0c540") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `notes-table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `note-name` TEXT NOT NULL, `note-description` TEXT NOT NULL, `pinned` INTEGER NOT NULL, `encrypted` INTEGER NOT NULL, `created_at` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3b789c318a699e5ce7dfb3e9fbdf0053')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `notes-table`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsNotesTable: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNotesTable.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotesTable.put("note-name", TableInfo.Column("note-name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotesTable.put("note-description", TableInfo.Column("note-description", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotesTable.put("pinned", TableInfo.Column("pinned", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotesTable.put("encrypted", TableInfo.Column("encrypted", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotesTable.put("created_at", TableInfo.Column("created_at", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNotesTable: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesNotesTable: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoNotesTable: TableInfo = TableInfo("notes-table", _columnsNotesTable,
            _foreignKeysNotesTable, _indicesNotesTable)
        val _existingNotesTable: TableInfo = read(connection, "notes-table")
        if (!_infoNotesTable.equals(_existingNotesTable)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |notes-table(com.kin.easynotes.domain.model.Note).
              | Expected:
              |""".trimMargin() + _infoNotesTable + """
              |
              | Found:
              |""".trimMargin() + _existingNotesTable)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "notes-table")
  }

  public override fun clearAllTables() {
    super.performClear(false, "notes-table")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(NoteDao::class, NoteDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun noteDao(): NoteDao = _noteDao.value
}
