package com.remind.me.doc.controller;

import com.github.messenger4j.Messenger;
import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.github.messenger4j.exception.MessengerVerificationException;
import com.github.messenger4j.messengerprofile.MessengerSettingProperty;
import com.github.messenger4j.send.MessagePayload;
import com.github.messenger4j.send.MessagingType;
import com.github.messenger4j.send.NotificationType;
import com.github.messenger4j.send.message.TextMessage;
import com.github.messenger4j.send.recipient.IdRecipient;
import com.github.messenger4j.webhook.Event;
import com.github.messenger4j.webhook.event.AccountLinkingEvent;
import com.github.messenger4j.webhook.event.MessageEchoEvent;
import com.github.messenger4j.webhook.event.PostbackEvent;
import com.github.messenger4j.webhook.event.TextMessageEvent;
import com.remind.me.doc.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.time.Instant;

import static com.github.messenger4j.Messenger.*;
import static com.github.messenger4j.Messenger.SIGNATURE_HEADER_NAME;
import static java.util.Optional.empty;
import static java.util.Optional.of;

@RestController
@RequestMapping("/callback")
public class MessengerPlatformCallbackHandler {

  private static final Logger logger = LoggerFactory.getLogger(MessengerPlatformCallbackHandler.class);

  private final Messenger messenger;

  @Value("${redirect.uri}")
  private String redirectUrl;
  @Value("${drchrono.clientId}")
  private String clientId;
  @Value("${drchrono.clientSecret}")
  private String clientSecret;
  @Value("${botId}")
  private String botId;
  @Value("${take,pills.message}")
  private String takePillsMessage;
  @Value("${did.not.take.pills}")
  private String didNotTakePillsMessage;
  @Value("${appointmentButtonCheckUp}")
  private String appointmentButtonCheckUp;


  @Autowired
  private MessengerService messengerService;

  @Autowired
  private AuthorizeService authorizeService;

  @Autowired
  private TakingMedicineService takingMedicineService;

  @Autowired
  private AppointmentService appointmentService;

  @Autowired
  private ResultsService resultsService;

  @Autowired
  private PatientService patientService;

  @Autowired
  public MessengerPlatformCallbackHandler(final Messenger messenger) {
    this.messenger = messenger;
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<String> verifyWebhook(@RequestParam(MODE_REQUEST_PARAM_NAME) final String mode,
                                              @RequestParam(VERIFY_TOKEN_REQUEST_PARAM_NAME) final String verifyToken, @RequestParam(CHALLENGE_REQUEST_PARAM_NAME) final String challenge) {
    logger.debug("Received Webhook verification request - mode: {} | verifyToken: {} | challenge: {}", mode, verifyToken, challenge);
    try {
      this.messenger.verifyWebhook(mode, verifyToken);
      messengerService.addMenuSettings();
      return ResponseEntity.ok(challenge);
    } catch (MessengerVerificationException e) {
      logger.warn("Webhook verification failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }
  }

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<Void> handleCallback(@RequestBody final String payload, @RequestHeader(SIGNATURE_HEADER_NAME) final String signature) {
    logger.debug("Received Messenger Platform callback - payload: {} | signature: {}", payload, signature);
    try {
      this.messenger.onReceiveEvents(payload, of(signature), event -> {
        if (event.isTextMessageEvent()) {
          handleTextMessageEvent(event.asTextMessageEvent());
        } else if (event.isPostbackEvent()) {
          handlePostbackEvent(event.asPostbackEvent());
        } else if (event.isMessageEchoEvent()) {
          handleMessageEchoEvent(event.asMessageEchoEvent());
        } else {
          handleFallbackEvent(event);
        }
      });
      logger.debug("Processed callback payload successfully");
      return ResponseEntity.status(HttpStatus.OK).build();
    } catch (MessengerVerificationException e) {
      logger.warn("Processing of callback payload failed: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
  }


  private void handleTextMessageEvent(TextMessageEvent event) {
    logger.debug("Received TextMessageEvent: {}", event);

    final String messageId = event.messageId();
    final String messageText = event.text();
    final String senderId = event.senderId();
    final Instant timestamp = event.timestamp();

    logger.info("Received message '{}' with text '{}' from user '{}' at '{}'", messageId, messageText, senderId, timestamp);

    try {
      switch (messageText.toLowerCase()) {

        case "get started":
          messengerService.sendGreetingMessage(senderId);
          messenger.deleteSettings(MessengerSettingProperty.PERSISTENT_MENU);
          break;
        case "menu":
          messengerService.messageMenu(senderId);
          break;
        case "delete":
          patientService.deletePatient(senderId);
          break;
        default:
          sendTextMessage(senderId, messageText);
      }
    } catch (MessengerApiException | MessengerIOException e) {
      handleSendException(e);
    }
  }

  private void handleMessageEchoEvent(MessageEchoEvent event) {
    logger.debug("Handling MessageEchoEvent");
    final String senderId = event.senderId();
    logger.debug("senderId: {}", senderId);
    final String recipientId = event.recipientId();
    logger.debug("recipientId: {}", recipientId);
    final String messageId = event.messageId();
    logger.debug("messageId: {}", messageId);
    final Instant timestamp = event.timestamp();
    logger.debug("timestamp: {}", timestamp);
    if (event.metadata().isPresent()) {
      try {
        switch (event.metadata().get()) {
          case "CHECK_AUTHORIZE":
            logger.info("check authorize");
            break;
          case "GET_STARTED_PAYLOAD":
            messengerService.sendGreetingMessage(recipientId);
            break;
          case "LIST_MEDICATIONS":
            messengerService.sendReminderTakePills(recipientId);
            break;
          case "MENU":
            messengerService.messageMenu(recipientId);
            break;
          case "RESULT":
            messengerService.buttonProgress(recipientId);
            break;
          case "SEND_RESULTS":
            messengerService.sendButtonChangeTimeAfterResults(recipientId);
            break;

          default:
            break;
        }
      } catch (MessengerApiException | MessengerIOException e) {
        handleSendException(e);
      }
    }
    logger.info("Received echo for message '{}' that has been sent to recipient '{}' by sender '{}' at '{}'", messageId, recipientId, senderId, timestamp);
  }

  private void handleAccountLinkingEvent(AccountLinkingEvent event) {
    logger.debug("Handling AccountLinkingEvent");
    final String senderId = event.senderId();
    logger.debug("senderId: {}", senderId);
    final AccountLinkingEvent.Status accountLinkingStatus = event.status();
    logger.debug("accountLinkingStatus: {}", accountLinkingStatus);
    final String authorizationCode = event.authorizationCode().orElse("Empty authorization code!!!"); //You can throw an Exception
    logger.debug("authorizationCode: {}", authorizationCode);
    logger.info("Received account linking event for user '{}' with status '{}' and auth code '{}'", senderId, accountLinkingStatus, authorizationCode);
    sendTextMessage(senderId, "AccountLinking event tapped");
  }

  private void handlePostbackEvent(PostbackEvent event) {
    logger.debug("Handling PostbackEvent");
    final String payload = event.payload().orElse("empty payload");
    logger.debug("payload: {}", payload);
    final String senderId = event.senderId();
    logger.debug("senderId: {}", senderId);
    final Instant timestamp = event.timestamp();
    logger.debug("timestamp: {}", timestamp);
    try {
      switch (payload) {
        case "AUTHORIZATION_PAYLOAD":
          messengerService.sendAuthorizeFormMessage(senderId);
          break;
        case "Get started":
          messengerService.sendGreetingMessage(senderId);
          break;
        case "MEDICINE_PAYLOAD":
          takingMedicineService.checkDiagnosis(senderId);
          break;
        case "PROCEED_MEDICATION_PAYLOAD":
          takingMedicineService.sendMessageAboutMedications(senderId);
          break;
        case "REMIND_PILLS_PAYLOAD":
          takingMedicineService.sendMessageAboutMedication(senderId);
          break;
        case "MENU_PAYLOAD":
          messengerService.messageMenu(senderId);
          break;
        case "REMIND_ONE_PILLS_PAYLOAD":
          messengerService.chooseTimeReminder(senderId);
          break;
        case "SET_REMINDER_TIME_PAYLOAD":
          messengerService.messageRemindTakePills(senderId);
          break;
        case "ACCEPT_PAYLOAD":
          authorizeService.authorizePatient(senderId);
          break;
        case "DID_IT_PAYLOAD":
          messengerService.chechTakingPillsButtons(senderId);
          break;
        case "TEST_PAYLOAD":
          messengerService.sendMessageAboutTest(senderId);
          break;
        case "TAKE_MEDICATION_PAYLOAD":
          messengerService.sendTextMessageWithMetadata(senderId, takePillsMessage, "RESULT");
          break;
        case "DID_NOT_TAKE_MEDICATION_PAYLOAD":
          messengerService.sendTextMessageWithMetadata(senderId, didNotTakePillsMessage, "RESULT");
          break;
        case "CHECK_UP_PAYLOAD":
          messengerService.sendCheckUpButton(senderId);
          break;
        case "CHECK_UP_RESULTS_PAYLOAD":
          messengerService.sendResultsCheckUp(senderId);
          break;
        case "CREATE_APPOINTMENT_PAYLOAD":
          messengerService.sendButtonSendToDoctor(senderId, appointmentButtonCheckUp);
          break;
        case "RESULTS_PAYLOAD":
          resultsService.sendResults(senderId);
          break;

        default:
          break;
      }
    } catch (MessengerApiException | MessengerIOException | MalformedURLException e) {
      handleSendException(e);
    }
    logger.info("Received postback for user '{}' and page '{}' with payload '{}' at '{}'", senderId, senderId, payload, timestamp);
    // sendTextMessage(senderId, "Postback event tapped");
  }


  private void handleSendException(Exception e) {
    logger.error("Message could not be sent. An unexpected error occurred.", e);
  }

  private void sendTextMessage(String recipientId, String text) {
    try {
      final IdRecipient recipient = IdRecipient.create(recipientId);
      final NotificationType notificationType = NotificationType.REGULAR;
      final String metadata = "DEVELOPER_DEFINED_METADATA";

      final TextMessage textMessage = TextMessage.create(text, empty(), of(metadata));
      final MessagePayload messagePayload = MessagePayload.create(recipient, MessagingType.RESPONSE, textMessage,
              of(notificationType), empty());
      this.messenger.send(messagePayload);
    } catch (MessengerApiException | MessengerIOException e) {
      handleSendException(e);
    }
  }

  private void handleFallbackEvent(Event event) {
    logger.debug("Handling FallbackEvent");
    final String senderId = event.senderId();
    logger.debug("senderId: {}", senderId);
    logger.info("Received unsupported message from user '{}'", senderId);
  }

}
