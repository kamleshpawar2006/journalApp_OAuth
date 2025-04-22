package net.engineeringdigest.journalApp.repositories;

import net.engineeringdigest.journalApp.entity.JournalEntity;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JournalRowMapper implements RowMapper<JournalEntity> {

    @Override
    public JournalEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
        JournalEntity journal = new JournalEntity();
        journal.setId(rs.getInt("id"));
        journal.setTitle(rs.getString("title"));
        journal.setContent(rs.getString("content"));
        return journal;
    }
}