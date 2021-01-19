package com.remind.me.doc.service;

import com.remind.me.doc.model.HealthTest;
import com.remind.me.doc.repository.HealthTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class HealthTestService {
  @Autowired
  HealthTestRepository healthTestRepository;


  public HealthTest getHealthTest(Integer id) {
    if (healthTestRepository.exists(id)) {
      return  healthTestRepository.findOne(id);
    } else throw new EntityNotFoundException();
  }


  public HealthTest createHealthTest(HealthTest healthTest) {
    return healthTestRepository.save(healthTest);
  }

  public void saveHealthTest(HealthTest healthTest) throws EntityNotFoundException {
    if(healthTestRepository.exists(healthTest.getId())){
      healthTestRepository.save(healthTest);}
    else  throw new EntityNotFoundException();
  }

  public void deleteHealthTest(Integer id){
    healthTestRepository.delete(id);
  }
}
