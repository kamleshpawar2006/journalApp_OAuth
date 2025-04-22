package net.engineeringdigest.journalApp.repositories;

import net.engineeringdigest.journalApp.entity.JournalEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class JournalRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;


    // @RequiredArgsConstructor can be used on this class and remove below code
    public JournalRepository(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public List<JournalEntity> getJournalEntity(String title) {
        String sql = "SELECT id, title, content FROM journal_entries WHERE title LIKE :title";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("title", "%" + title + "%");

        return namedParameterJdbcTemplate.query(
                sql,
                params,
                new JournalRowMapper()
        );
    }



    public List<JournalEntity> getJournalEntity(int id) {
        String sql = "SELECT id, title, content FROM journal_entries WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return namedParameterJdbcTemplate.query(
                sql,
                params,
                new JournalRowMapper()
        );
    }

    public List<JournalEntity> getJournalEntriesByIDs(List<Integer> journalEntriesIDs) {
        String sql = "SELECT id, title, content FROM journal_entries WHERE id in (:journalEntriesIDs)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("journalEntriesIDs", journalEntriesIDs);

        return namedParameterJdbcTemplate.query(
                sql,
                params,
                new JournalRowMapper()
        );
    }

    public List<JournalEntity> findAll() {
        String sql = "SELECT * FROM journal_entries";
        // this is better than the below approach
        return jdbcTemplate.query(sql, new JournalRowMapper());

// this is second approach, simple but not recommended for enterprise level implementation
//        return jdbcTemplate.query(sql, (rs, rowNum) -> {
//            JournalEntity journal = new JournalEntity();
//            journal.setId(rs.getInt("id"));
//            journal.setTitle(rs.getString("title"));
//            journal.setContent(rs.getString("content"));
//            return journal;
//        });

    }

    public int save(JournalEntity journal) {

          // Normal JdbcTemplate way:
//        String sql = "INSERT INTO journal_entries (title, content) VALUES (?, ?)";
//        jdbcTemplate.update(sql, journal.getTitle(), journal.getContent());
//
          // Only NamedParameterJdbcTemplate way:
//        String sql = "INSERT INTO journal_entries (title, content) VALUES (:title, :content)";
//        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("title", journal.getTitle());
//        paramMap.put("content", journal.getContent());
//        namedParameterJdbcTemplate.update(sql, paramMap);

        // NamedParameterJdbcTemplate + BeanPropertySqlParameterSource way:
        String sql = "INSERT INTO journal_entries (title, content) VALUES (:title, :content)";
        BeanPropertySqlParameterSource paramSource = new BeanPropertySqlParameterSource(journal);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, paramSource, keyHolder, new String[]{"id"});

        return keyHolder.getKey().intValue();
    }

    public List<Integer> saveAll(List<JournalEntity> journalList) {

        if (journalList == null || journalList.isEmpty()) {
            return null;
        }

//        String sql = "INSERT INTO journal_entries (title, content) VALUES (:title, :content)";
//
//        SqlParameterSource[] batchParams = journalList
//                .stream()
//                .map(BeanPropertySqlParameterSource::new)
//                .toArray(SqlParameterSource[]::new);
//
//        /*  In simple terms - below is the easier implementation of above code
//        SqlParameterSource[] batchParams = new SqlParameterSource[journalList.size()];
//
//        for (int i = 0; i < journalList.size(); i++) {
//            batchParams[i] = new BeanPropertySqlParameterSource(journalList.get(i));
//        }
//         */
//       below is not inserted IDs
//       int insertedRecords[] = namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
//       return insertedRecords;

        List<Integer> insertedIds = new ArrayList<>();
        String sql = "INSERT INTO journal_entries (title, content) VALUES (:title, :content)";
        for (JournalEntity journal: journalList) {
            BeanPropertySqlParameterSource paramSource = new BeanPropertySqlParameterSource(journal);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            namedParameterJdbcTemplate.update(sql, paramSource, keyHolder, new String[]{"id"});
            Number generatedId = keyHolder.getKey();
            if(generatedId != null) {
                insertedIds.add(generatedId.intValue());
            }
        }

        return insertedIds;
    }

    public int[] updateAll(List<JournalEntity> journalList) throws IllegalArgumentException {

        if (journalList == null || journalList.isEmpty()) {
            return null;
        }

        String sql = "UPDATE journal_entries " +
                "SET title = COALESCE(:title, title), " +
                "content = COALESCE(:content, content) " +
                "WHERE id = :id";

        SqlParameterSource[] batchParams = journalList.stream()
                .map(journal -> {
                    MapSqlParameterSource params = new MapSqlParameterSource();
                    if (journal.getId() == null) {
                        throw new IllegalArgumentException("JournalEntity must have a non-null ID for update: " + journal);
                    }
                    params.addValue("id", journal.getId());
                    params.addValue("title", journal.getTitle());   // can be null, will skip update
                    params.addValue("content", journal.getContent()); // can be null, will skip update
                    return params;
                })
                .toArray(SqlParameterSource[]::new);

        return namedParameterJdbcTemplate.batchUpdate(sql, batchParams);
    }

    public int deleteJournal(Integer id) {
        String sql = "DELETE FROM journal_entries WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        return namedParameterJdbcTemplate.update(
            sql,
            params
        );
    }
}