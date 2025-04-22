package net.engineeringdigest.journalApp.controller;

import lombok.extern.slf4j.Slf4j;
import net.engineeringdigest.journalApp.entity.JournalEntity;
import net.engineeringdigest.journalApp.entity.UserEntity;
import net.engineeringdigest.journalApp.exception.ResourceNotFoundException;
import net.engineeringdigest.journalApp.service.JournalService;
import net.engineeringdigest.journalApp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/journal")
public class JournalController {

    @Autowired
    private JournalService journalService;

    @Autowired
    private UserService userService;

    private List<JournalEntity> journalEntities = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<JournalEntity>> journalEntries() {
        return new ResponseEntity<>(journalService.getAllJournalEntries(), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<JournalEntity>> journalEntriesByUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userService.findByUserName(authentication.getName());
        List<Integer> journalEntriesIDs = user.getJournalEntries();
        if(journalEntriesIDs != null && journalEntriesIDs.size() > 0) {
            List<JournalEntity> journalEntities = journalService.getJournalEntriesByIDs(journalEntriesIDs);
            return new ResponseEntity<>(journalEntities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> journalEntriesByCriteria(@RequestParam String criteria, @RequestParam String criteriaValue) {
        List<JournalEntity> result;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userService.findByUserName(authentication.getName());
        List<Integer> journalEntriesIDs = user.getJournalEntries();

        switch (criteria)  {
            case "id" : {
                try {
                    Integer journalId = Integer.parseInt(criteriaValue);
                    if(journalEntriesIDs.contains(journalId)) {
                        result = journalService.getJournalEntity(journalId);
                    } else {
                        return new ResponseEntity<>("Unauthorised Journal Entry", HttpStatus.BAD_REQUEST);
                    }
                } catch (NumberFormatException ex) {
                    return new ResponseEntity<>("Invalid search value", HttpStatus.BAD_REQUEST);
                }
                break;
            }
            case "title" : {
                result = journalService.getJournalEntity(criteriaValue);
                break;
            }
            default: {
                return new ResponseEntity<>("Invalid search criteria", HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(result, (!result.isEmpty()) ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<?> addJournal(@RequestBody JournalEntity journalEntity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userService.findByUserName(authentication.getName());
        Integer insertedId;
        try {
            insertedId = journalService.save(journalEntity, user.getUserName());
            journalEntity.setId(insertedId);
            return new ResponseEntity<>(journalEntity, HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/save-all-journals")
    public ResponseEntity<?> addAllJournal(@RequestBody List<JournalEntity> journalEntities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return new ResponseEntity<>(journalService.saveAll(journalEntities, authentication.getName()), HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<?> updateJournal(@RequestBody List<JournalEntity> journalEntityList) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userService.findByUserName(authentication.getName());
        if(authentication.getName() != null && journalEntityList.size() > 0) {
            try {
                List<Integer> journalEntriesIDs = user.getJournalEntries();
                List<JournalEntity> authenticatedJournalList = journalEntityList.stream().filter(e -> journalEntriesIDs.contains(e.getId())).collect(Collectors.toList());
                List<JournalEntity> unAuthenticatedJournalList = journalEntityList.stream().filter(e -> !journalEntriesIDs.contains(e.getId())).collect(Collectors.toList());
                Map<String, List<Integer>> journalIDs = new HashMap<>();

                if(authenticatedJournalList.size() > 0) {
                    journalIDs.put("Authenticated Journal IDs", authenticatedJournalList.stream().map(e -> e.getId()).collect(Collectors.toList()));
                    journalService.updateAll(authenticatedJournalList);
                }
                if(unAuthenticatedJournalList.size() > 0) {
                    journalIDs.put("Unauthenticated Journal IDs", unAuthenticatedJournalList.stream().map(e -> e.getId()).collect(Collectors.toList()));
                }
                return new ResponseEntity<>(journalIDs, HttpStatus.OK);
            } catch (IllegalArgumentException ex) {
                log.error(String.valueOf(ex));
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } catch (Exception ex) {
                log.error(String.valueOf(ex));
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJournal(@PathVariable Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity user = userService.findByUserName(authentication.getName());
        List<Integer> journalEntriesIDs = user.getJournalEntries();
        if(journalEntriesIDs.contains(id)) {
            try {
                return new ResponseEntity<>(journalService.deleteJournal(id), HttpStatus.OK);
            } catch (RuntimeException ex) {
                log.error(ex.getMessage());
                return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>("Journal not found", HttpStatus.NOT_FOUND);
        }
    }

}
