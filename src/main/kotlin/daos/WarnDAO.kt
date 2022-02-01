package daos

import com.j256.ormlite.dao.Dao
import com.j256.ormlite.dao.DaoManager
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.TableUtils
import systems.warnsystem.Warn
import util.FileAble

class WarnDAO(var connection: ConnectionSource): DAO<Warn> {

    private val dao: Dao<Warn, String> = DaoManager.createDao(connection, Warn::class.java)

    fun getWarnsForMember(member: Long): List<Warn> {
        val queryBuilder = dao.queryBuilder()
        return queryBuilder.where().ge("memberID",member).query()
    }

    override fun initTable() {
        TableUtils.createTableIfNotExists(connection, Warn::class.java)
    }

    override fun create(obj: Warn) {
    }

}