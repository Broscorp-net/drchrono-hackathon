package service;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.common.WebviewHeightRatio;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.messengerprofile.MessengerSettings;
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
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${diagnosis.message}")
  private String diagnosisMessage;

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
                    empty(),
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
    final List<Button> buttons = Arrays.asList(
            UrlButton.create("Choose", new URL(redirectUrl + "/medication/68465379/date/" + senderId), of(WebviewHeightRatio.COMPACT), of(false), empty(), empty())
            //     PostbackButton.create("Fill form", "AUTHORIZATION_FORM_PAYLOAD")
    );


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

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Hi Alexis, it's a time to take ibuprofen!", buttons);
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

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Hi Alexis, it's a time to take ibuprofen!", buttons);
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
    String textMessage = String.format("Hi %s, did you take this pill today?", "Alexis");
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

    final ButtonTemplate buttonTemplate = ButtonTemplate.create("Hi, this is Dr.Chrono bot. I will help you to take your medication on time, schedule recommended check-ups, get tested and stay healthy.", buttons);
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
}
