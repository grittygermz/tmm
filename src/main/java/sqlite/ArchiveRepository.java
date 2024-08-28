package sqlite;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.extern.slf4j.Slf4j;
import sqlite.models.ArchivePack;
import sqlite.models.ArchiveSeq;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class ArchiveRepository {

    private String databaseUrl = "jdbc:sqlite:sample.db";
    private Dao<ArchivePack, String> archivePackDao;
    private Dao<ArchiveSeq, String> archiveSeqDao;

    public void setupDatabase() {
        try (ConnectionSource connectionSource = new JdbcConnectionSource(databaseUrl)) {

            // instantiate the dao
            archivePackDao = DaoManager.createDao(connectionSource, ArchivePack.class);
            archiveSeqDao = DaoManager.createDao(connectionSource, ArchiveSeq.class);

            // create table if not exists
            TableUtils.createTableIfNotExists(connectionSource, ArchivePack.class);
            TableUtils.createTableIfNotExists(connectionSource, ArchiveSeq.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getLatestSeqNumToUse(String packPrefix) {
        QueryBuilder<ArchivePack, String> archivePackStringQueryBuilder = archivePackDao.queryBuilder();
        archivePackStringQueryBuilder.orderBy("seqEnd", false).limit(1L);
        List<ArchivePack> query;
        try {
             query = archivePackDao.query(archivePackStringQueryBuilder.prepare());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(query.size() < 1) {
            log.info("packPrefix {} is not previously existing", packPrefix);
            return 0;
        }
        return query.get(0).getSeqEnd() + 1;
    }

    public int createArchivePackEntry(ArchivePack archivePack) {
        int archivePackId;
        try {
            archivePackId = archivePackDao.create(archivePack);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return archivePackId;
    }

    public void updateArchivePackEntry(ArchivePack archivePack) {
        try {
            archivePackDao.update(archivePack);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createArchiveSeqEntry(ArchiveSeq archiveSeq) {
        try {
            archiveSeqDao.create(archiveSeq);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
