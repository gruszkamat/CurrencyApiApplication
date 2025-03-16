package org.mgruszka.currency.repository;

import org.mgruszka.currency.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AccountRepository extends MongoRepository<Account, String> {}
