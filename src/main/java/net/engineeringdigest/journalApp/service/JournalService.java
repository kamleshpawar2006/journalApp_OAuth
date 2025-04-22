package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.entity.JournalEntity;
import net.engineeringdigest.journalApp.entity.UserEntity;
import net.engineeringdigest.journalApp.exception.ResourceNotFoundException;
import net.engineeringdigest.journalApp.repositories.JournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    @Autowired
    private UserService userService;

    public List<JournalEntity> getAllJournalEntries() {
        return journalRepository.findAll();
    }

    public List<JournalEntity> getJournalEntriesByIDs(List<Integer> journalEntriesIDs) {
        return journalRepository.getJournalEntriesByIDs(journalEntriesIDs);
    }

    public List<JournalEntity> getJournalEntity(String title) {
        return journalRepository.getJournalEntity(title);
    }

    public List<JournalEntity> getJournalEntity(int id) {
        return journalRepository.getJournalEntity(id);
    }

    public int save(JournalEntity journalEntity, String userName) throws ResourceNotFoundException {
        int insertedId = journalRepository.save(journalEntity);
        List<Integer> insertedIDs = new ArrayList<>();
        insertedIDs.add(insertedId);
        saveJournalIDsOfUser(insertedIDs, userName);
        return insertedId;
    }

    public List<Integer> saveAll(List<JournalEntity> journalEntities, String userName) {
        List<Integer> insertedIDs = journalRepository.saveAll(journalEntities);
        saveJournalIDsOfUser(insertedIDs, userName);
        return insertedIDs;
    }

    private void saveJournalIDsOfUser(List<Integer> insertedIDs, String userName) {
        UserEntity user = userService.findByUserName(userName);
        if(user == null) {
            throw new ResourceNotFoundException("User not found with username: " + userName);
        }
        List<Integer> journalEntriesIDs = new ArrayList<>();
        if(user.getJournalEntries() != null && user.getJournalEntries().size() > 0) {
            journalEntriesIDs.addAll(user.getJournalEntries());
        }
        journalEntriesIDs.addAll(insertedIDs);
        user.setJournalEntries(journalEntriesIDs);
        userService.saveUserNotPassword(user);
    }

    public int[] updateAll(List<JournalEntity> journalEntities) throws IllegalArgumentException {
        return journalRepository.updateAll(journalEntities);
    }

    @Transactional
    public int deleteJournal(Integer id) {
        int deleted = journalRepository.deleteJournal(id);
        if(deleted != 1) {
            throw new RuntimeException("Journal not found that needs to be deleted journalId: " + id);
        }
        boolean journalDeletedFromUser = deleteJournalFromUser(id);
        if(journalDeletedFromUser) {
            return 1;
        } else {
            throw new RuntimeException("Cannot delete journal from Users table");
        }
    }

    private boolean deleteJournalFromUser(Integer id) {
        Integer userId = userService.getUserIdByJournalId(id);
        if(userId == null) {
            throw new IllegalStateException("Data inconsistent. Journal Id not present in users table. journalID: " + id);
        }
        UserEntity user = userService.findById(userId);
        if(user.getJournalEntries() != null && user.getJournalEntries().size() > 0) {
            List<Integer> journalIDs = user.getJournalEntries();
            journalIDs.remove(id);
            user.setJournalEntries(journalIDs);
            userService.saveUserNotPassword(user);
            return true;
        }
        return false;
    }

}
