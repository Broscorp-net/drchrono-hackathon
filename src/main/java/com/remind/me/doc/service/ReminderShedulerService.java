package com.remind.me.doc.service;

import com.remind.me.doc.model.Medication;
import com.remind.me.doc.model.Patient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

@EnableScheduling
@Service
public class ReminderShedulerService {

  @Autowired
  private MessengerService messengerService;
  @Autowired
  private PatientService patientService;

  TaskScheduler scheduler;

  Map<Integer, ScheduledFuture<?>> jobsMap = new HashMap<>();

  public ReminderShedulerService(TaskScheduler scheduler) {
    this.scheduler = scheduler;
  }

  public void addTaskToScheduler(int id, Runnable task, String cron) {
    ScheduledFuture<?> scheduledTask = scheduler.schedule(task, new CronTrigger(cron, TimeZone.getTimeZone(TimeZone.getDefault().getID())));
    jobsMap.put(id, scheduledTask);
  }

  public void removeTaskFromScheduler(int id) {
    ScheduledFuture<?> scheduledTask = jobsMap.get(id);
    if(scheduledTask != null) {
      scheduledTask.cancel(true);
      jobsMap.put(id, null);
    }
  }

  @EventListener({ ContextRefreshedEvent.class })
  void contextRefreshedEvent() {
    // Get all tasks from DB and reschedule them in case of context restarted
    createTask();
  }


  public String getCron(Medication medication){
    LocalDateTime dateTime = medication.getReminderTime();
    return  toCron(String.valueOf(dateTime.getMinute()),
            String.valueOf(dateTime.getHour()));
  }


  public static String toCron(final String mins, final String hrs) {
    return String.format("0 %s %s * * *", mins, hrs);
  }

  public void createTask(){
    List<Patient> patients = patientService.listPatient();
    int id = 1;
    for (Patient patient: patients) {
      List<Medication> medications = patient.getMedicationList();
      for (Medication medication: medications) {
        String cron = "0 0 17 * * *";
        if (medication.getReminderTime() != null) {
          cron = getCron(medication);
        }
        addTaskToScheduler(id, () -> messengerService.messageRemindTakePillsWithPillsName(patient.getIdFacebook(), medication), cron);
      }
      addTaskToScheduler(id, () -> messengerService.chechTakingPillsButtons(patient.getIdFacebook()), "* 0 21 * * *");
    }
  }
}
