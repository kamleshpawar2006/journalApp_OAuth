package net.engineeringdigest.journalApp.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @Column(name = "userName", nullable = false, length = 45)
    private String userName;

    @Column(name = "password", nullable = false, length = 300)
    private String password;

    @Column(name = "journal_entries", length = 300)
    private String journalEntries;

    @Column(name = "roles")
    private String roles;

    // Custom Getter: Convert CSV String to List<Integer>
    public List<Integer> getJournalEntries() {
        if (journalEntries == null || journalEntries.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(journalEntries.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    // Custom Setter: Convert List<Integer> to CSV String
    public void setJournalEntries(List<Integer> journalIds) {
        if (journalIds == null || journalIds.isEmpty()) {
            this.journalEntries = null;
        } else {
            this.journalEntries = journalIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    public List<String> getRoles() {
        if (roles == null || roles.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(roles.split(","));
    }

    public void setRoles(String... roleList) {
        if (roleList == null || roleList.length == 0) {
            this.roles = null;
        } else {
            this.roles = String.join(",", roleList);
        }
    }
}
