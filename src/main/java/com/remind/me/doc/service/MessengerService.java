package com.remind.me.doc.service;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.common.WebviewHeightRatio;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.messengerprofile.MessengerSettings;
import com.github.messenger4j.messengerprofile.getstarted.StartButton;
import com.github.messenger4j.messengerprofile.persistentmenu.PersistentMenu;
import com.github.messenger4j.messengerprofile.persistentmenu.action.PostbackCallToAction;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.message.TemplateMessage;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.message.template.ButtonTemplate;
import com.github.messenger4j.send.message.template.button.Button;
import com.github.messenger4j.send.message.template.button.PostbackButton;
import com.github.messenger4j.send.message.template.button.UrlButton;
import com.github.messenger4j.send.recipient.IdRecipient;
import com.remind.me.doc.model.Medication;
import com.remind.me.doc.model.Patient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Service
public class MessengerService {

  @Autowired
  private Messenger messenger;
  @Autowired
  private PatientService patientService;
  @Autowired
  private MedicationService medicationService;


  @Value("${diagnosis.message}")
  private String diagnosisMessage;
  @Value("${letstest.message}")
  private String letstestMessage;
  @Value("${letstestButton.message}")
  private String letstestButtonMessage;
  @Value("${greeting.message}")
  private String greetingMessage;
  @Value("${checkUp.message}")
  private String checkUpMessage;

  @Value("${redirect.uri}")
  private String redirectUrl;




  public void sendTextMessage(String recipientId, String text) {
    try {
      final IdRecipient recipient = IdRecipient.create(recipientId);
      final NotificationType notificationType = NotificationType.REGULAR;

      final TextMessage textMessage = TextMessage.create(text, empty(), empty());
      final MessagePayload messagePayload = MessagePayload.create(recipient, MessagingType.RESPONSE, textMessage,
              of(notificationType), empty());
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void sendTextMessageWithMetadata(String recipientId, String text, String metadata) {
    try {
      final IdRecipient recipient = IdRecipient.create(recipientId);
      final NotificationType notificationType = NotificationType.REGULAR;

      final TextMessage textMessage = TextMessage.create(text, empty(), of(metadata));
      final MessagePayload messagePayload = MessagePayload.create(recipient, MessagingType.RESPONSE, textMessage,
              of(notificationType), empty());
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  @SneakyThrows
  public void addMenuSettings(){

    final PostbackCallToAction callToActionAA =
            PostbackCallToAction.create("Taking medicine", "MEDICINE_PAYLOAD");
    final PostbackCallToAction callToActionAB =
            PostbackCallToAction.create("Get tested", "TEST_PAYLOAD");
    final PostbackCallToAction callToActionAC =
            PostbackCallToAction.create("Check up", "CHECK_UP_PAYLOAD");

    final PersistentMenu persistentMenu =
            PersistentMenu.create(
                    false,
                    of(Arrays.asList(callToActionAA, callToActionAB, callToActionAC)));

    final MessengerSettings messengerSettings =
            MessengerSettings.create(
                    of(StartButton.create("Get started")),
                    empty(),
                    of(persistentMenu),
                    empty(),
                    empty(),
                    empty(),
                    empty());

    messenger.updateSettings(messengerSettings);
  }

  @SneakyThrows
  public void sendDiagnosisMessage(List<String> diagnosis, String senderId){
    StringBuilder sb = new StringBuilder();
    sb.append(diagnosisMessage);
    if (diagnosis.size() == 0) {
      sendTextMessage(senderId, "You had no problem. Congratulations!");
      return;
    }
    for (int i=0; i<diagnosis.size(); i++) {
      int number = i + 1;
      sb.append("\n" + number + ". " +  diagnosis.get(i) );
    }
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Proceed to medication", "PROCEED_MEDICATION_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create(sb.toString(), buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }


  public void sendReminderTakePills(String senderId) throws MessengerApiException, MessengerIOException {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Remind me to take pills", "REMIND_PILLS_PAYLOAD"),
            PostbackButton.create("Menu", "MENU_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Do you want me to remind you when to take pills?", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }

  public void sendButtonsAboutReminds(String senderId) throws MessengerApiException, MessengerIOException {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Set a reminder for this", "REMIND_ONE_PILLS_PAYLOAD"),
            PostbackButton.create("Set a reminder for all", "REMIND_ALL_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Do you want me to remind you when to take pills?", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }

  public void chooseTimeReminder(String senderId) throws MalformedURLException, MessengerApiException, MessengerIOException {
    Patient patient = patientService.getPatient(senderId);

    final List<Button> buttons = Arrays.asList(
            UrlButton.create("Choose", new URL(redirectUrl + "/medication/" + patient.getCurrentMedication() + "/date/" + senderId), of(WebviewHeightRatio.COMPACT), of(false), empty(), empty())
    );
    //68465379

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Choose the time I'll remind you every day.", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);

  }

  public void addSetReminderButton(String senderId) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Cool", "SET_REMINDER_TIME_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Got it! Reminder is set!", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void messageRemindTakePills(String senderId) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Reminde me a dose", "REMIND_PILLS_PAYLOAD"),
            PostbackButton.create("Did it", "DID_IT_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Hi Alexis, it's a time to take pills!", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void messageRemindTakePillsWithPillsName(String senderId, Medication medication) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Reminde me a dose", "REMIND_PILLS_PAYLOAD"),
            PostbackButton.create("Did it", "DID_IT_PAYLOAD")
    );
    String message = String.format("Hi, %s, it's a time to take %s!", medication.getPatient().getFirstName(),
            medication.getName());
    final ButtonTemplate buttonTemplate = ButtonTemplate.create(message, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void messageMenu(String senderId) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Taking medicine", "MEDICINE_PAYLOAD"),
            PostbackButton.create("Get tested", "TEST_PAYLOAD"),
            PostbackButton.create("Check up", "CHECK_UP_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Menu", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void sendButtonMessageAboutForm(String senderId, String textMessage) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Yes", "ACCEPT_PAYLOAD"),
            PostbackButton.create("No", "AUTHORIZATION_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create(textMessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void chechTakingPillsButtons(String senderId) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Yes", "TAKE_MEDICATION_PAYLOAD"),
            PostbackButton.create("No", "DID_NOT_TAKE_MEDICATION_PAYLOAD")
    );
    Patient patient = patientService.getPatient(senderId);
    for (Medication medication: patient.getMedicationList()) {
      medication.setTakingPills(medication.getTakingPills() + 1);
      medicationService.saveMedication(medication);
    }
    String textMessage = String.format("Hi %s, did you take this pill today?", patient.getFirstName());
    final ButtonTemplate buttonTemplate = ButtonTemplate.create(textMessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void sendGreetingMessage(String senderId) throws MessengerApiException, MessengerIOException {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Sounds good", "AUTHORIZATION_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create(greetingMessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }


  public void sendAuthorizeFormMessage(String senderId) throws MessengerApiException, MessengerIOException, MalformedURLException {
    final List<Button> buttons = Arrays.asList(
            UrlButton.create("Fill form", new URL(redirectUrl +"/patient/add/" + senderId), of(WebviewHeightRatio.COMPACT), of(false), empty(), empty())
    );


    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Tell me your name, surname and ID for verification.", buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }

  public void sendMessageAboutTest(String senderId) throws MalformedURLException, MessengerApiException, MessengerIOException {
    Patient patient = patientService.getPatient(senderId);
    String message = String.format(letstestMessage, patient.getFirstName());
    sendTextMessage(senderId, message);
    final List<Button> buttons = Arrays.asList(
            UrlButton.create("Do it", new URL(redirectUrl +"/healthTest/add/" + senderId), of(WebviewHeightRatio.TALL), of(false), empty(), empty()),
            PostbackButton.create("Skip it", "MENU_PAYLOAD")
    );

    final ButtonTemplate buttonTemplate = ButtonTemplate.create(letstestButtonMessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }

  public void sendButtonSendToDoctor(String idFacebook, String textMessage) {
    try {
      final List<Button> buttons;
      buttons = Arrays.asList(
              UrlButton.create("Schedule appointment", new URL(redirectUrl +"/appointment/" + idFacebook + "/add/" ), of(WebviewHeightRatio.COMPACT), of(false), empty(), empty()),
              PostbackButton.create("Skip it", "MENU_PAYLOAD")
      );
      final ButtonTemplate buttonTemplate = ButtonTemplate.create(textMessage, buttons);
      final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
      final MessagePayload messagePayload = MessagePayload.create(idFacebook, MessagingType.RESPONSE, templateMessage);
      this.messenger.send(messagePayload);
    } catch (MalformedURLException | MessengerIOException | MessengerApiException e) {
      e.printStackTrace();
    }
  }

  public void sendCheckUpButton(String senderId) throws MessengerApiException, MessengerIOException {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Let's do it", "CHECK_UP_RESULTS_PAYLOAD")
    );
    Patient patient = patientService.getPatient(senderId);
    String checkUpmessage = String.format("Hi, %s, I can help you to do your check up and to be as fit as a fiddle.",
            patient.getFirstName());
    final ButtonTemplate buttonTemplate = ButtonTemplate.create(checkUpmessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(senderId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }

  public void sendResultsCheckUp(String idFacebook) {
    try {
      final List<Button> buttons;
      buttons = Arrays.asList(
              PostbackButton.create("Schedule 1st", "CREATE_APPOINTMENT_PAYLOAD"),
              PostbackButton.create("Schedule all", "CREATE_APPOINTMENT_PAYLOAD"),
              PostbackButton.create("Skip it", "MENU_PAYLOAD")
      );
      final ButtonTemplate buttonTemplate = ButtonTemplate.create(checkUpMessage, buttons);
      final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
      final MessagePayload messagePayload = MessagePayload.create(idFacebook, MessagingType.RESPONSE, templateMessage);
      this.messenger.send(messagePayload);
    } catch (MessengerIOException | MessengerApiException e) {
      e.printStackTrace();
    }
  }

  public void buttonProgress(String recipientId) throws MessengerApiException, MessengerIOException {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Check it out", "RESULTS_PAYLOAD")
    );
    Patient patient = patientService.getPatient(recipientId);
    String resultMessage = String.format("Hi, %s, it's the end of the week, let's check your progress.",
            patient.getFirstName());
    final ButtonTemplate buttonTemplate = ButtonTemplate.create(resultMessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
    this.messenger.send(messagePayload);
  }

  public void sendButtonChangeTimeAfterResults(String recipientId) {
    final List<Button> buttons = Arrays.asList(
            PostbackButton.create("Change", "REMIND_ONE_PILLS_PAYLOAD"),
            PostbackButton.create("No", "MENU_PAYLOAD")
    );
    String textMessage = "Maybe you'd like to change time for reminders to hit your weekly goals?";
    final ButtonTemplate buttonTemplate = ButtonTemplate.create(textMessage, buttons);
    final TemplateMessage templateMessage = TemplateMessage.create(buttonTemplate);
    final MessagePayload messagePayload = MessagePayload.create(recipientId, MessagingType.RESPONSE, templateMessage);
    try {
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }
}
