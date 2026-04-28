package com.kin.easynotes.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performBlocking
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.kin.easynotes.domain.model.Note
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class NoteDao_Impl(
  __db: RoomDatabase,
) : NoteDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfNote: EntityInsertAdapter<Note>

  private val __deleteAdapterOfNote: EntityDeleteOrUpdateAdapter<Note>

  private val __updateAdapterOfNote: EntityDeleteOrUpdateAdapter<Note>
  init {
    this.__db = __db
    this.__insertAdapterOfNote = object : EntityInsertAdapter<Note>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `notes-table` (`id`,`note-name`,`note-description`,`pinned`,`encrypted`,`created_at`) VALUES (nullif(?, 0),?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: Note) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        val _tmp: Int = if (entity.pinned) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.encrypted) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindLong(6, entity.createdAt)
      }
    }
    this.__deleteAdapterOfNote = object : EntityDeleteOrUpdateAdapter<Note>() {
      protected override fun createQuery(): String = "DELETE FROM `notes-table` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Note) {
        statement.bindLong(1, entity.id.toLong())
      }
    }
    this.__updateAdapterOfNote = object : EntityDeleteOrUpdateAdapter<Note>() {
      protected override fun createQuery(): String =
          "UPDATE OR ABORT `notes-table` SET `id` = ?,`note-name` = ?,`note-description` = ?,`pinned` = ?,`encrypted` = ?,`created_at` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: Note) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.description)
        val _tmp: Int = if (entity.pinned) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmp_1: Int = if (entity.encrypted) 1 else 0
        statement.bindLong(5, _tmp_1.toLong())
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.id.toLong())
      }
    }
  }

  public override suspend fun addNote(note: Note): Unit = performSuspending(__db, false, true) {
      _connection ->
    __insertAdapterOfNote.insert(_connection, note)
  }

  public override suspend fun deleteNote(note: Note): Unit = performSuspending(__db, false, true) {
      _connection ->
    __deleteAdapterOfNote.handle(_connection, note)
  }

  public override suspend fun updateNote(note: Note): Unit = performSuspending(__db, false, true) {
      _connection ->
    __updateAdapterOfNote.handle(_connection, note)
  }

  public override fun getAllNotes(): Flow<List<Note>> {
    val _sql: String = "SELECT * FROM `notes-table`"
    return createFlow(__db, false, arrayOf("notes-table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "note-name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "note-description")
        val _columnIndexOfPinned: Int = getColumnIndexOrThrow(_stmt, "pinned")
        val _columnIndexOfEncrypted: Int = getColumnIndexOrThrow(_stmt, "encrypted")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _result: MutableList<Note> = mutableListOf()
        while (_stmt.step()) {
          val _item: Note
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpPinned: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPinned).toInt()
          _tmpPinned = _tmp != 0
          val _tmpEncrypted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfEncrypted).toInt()
          _tmpEncrypted = _tmp_1 != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = Note(_tmpId,_tmpName,_tmpDescription,_tmpPinned,_tmpEncrypted,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getNoteById(id: Int): Flow<Note?> {
    val _sql: String = "SELECT * FROM `notes-table` WHERE id=?"
    return createFlow(__db, false, arrayOf("notes-table")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "note-name")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "note-description")
        val _columnIndexOfPinned: Int = getColumnIndexOrThrow(_stmt, "pinned")
        val _columnIndexOfEncrypted: Int = getColumnIndexOrThrow(_stmt, "encrypted")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _result: Note?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpPinned: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfPinned).toInt()
          _tmpPinned = _tmp != 0
          val _tmpEncrypted: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfEncrypted).toInt()
          _tmpEncrypted = _tmp_1 != 0
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = Note(_tmpId,_tmpName,_tmpDescription,_tmpPinned,_tmpEncrypted,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getLastNoteId(): Long? {
    val _sql: String = "SELECT id FROM `notes-table` ORDER BY id DESC LIMIT 1"
    return performBlocking(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Long?
        if (_stmt.step()) {
          if (_stmt.isNull(0)) {
            _result = null
          } else {
            _result = _stmt.getLong(0)
          }
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
