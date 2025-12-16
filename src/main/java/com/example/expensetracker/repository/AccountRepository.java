// AccountRepository.java
package com.example.expensetracker.repository;

import com.example.expensetracker.model.Account;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByOwner(User owner);

    boolean existsByOwnerAndName(User owner, String name);

}
