package com.remind.me.doc.repository;

import com.remind.me.doc.model.HealthTest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthTestRepository extends CrudRepository<HealthTest, Integer>{
}
