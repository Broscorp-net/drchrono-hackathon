package service;

import model.Patient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class AuthorizeService {
  @Autowired
  MessengerService messengerService;

  @Value("${access.token}")
  private String accessToken;

  @Value("${dr.chrono.url}")
  private String defaultUrl;

  @Value("${provider.name}")
  private String providerName;

  @Autowired
  PatientService patientService;

  public void authorizePatient(String idFacebook){
    //   Patient patient = patientService.getPatient(idFacebook);
    Patient patient = new Patient();
    String textMessage = "Nice to meet you, " + patient.getFirstName() + " " + patient.getLastName() +
            "!\n Give me a sec...";
    messengerService.sendTextMessage(patient.getIdFacebook(), textMessage);
    int status = getPatient(patient);
    String message;
    if (status == 200) {
      message = patient.getFirstName()  + ", I've found that:\n" +
              "your provider is - " + providerName;
      messengerService.addMenuSettings();
    } else message = "Something went wrong. Try again";
    messengerService.sendTextMessageWithMetadata(patient.getIdFacebook(), message, "MENU");

  }

  public void authorizePatient(Patient patient){
    String textMessage = "Nice to meet you, " + patient.getFirstName() + " " + patient.getLastName() +
            "!\n Give me a sec...";
    messengerService.sendTextMessage(patient.getIdFacebook(), textMessage);
    int status = getPatient(patient);
    String message;
    if (status == 200) {
      message = patient.getFirstName()  + ", I've found that:\n" +
              "your provider is - " + providerName;
      messengerService.addMenuSettings();
    } else message = "Something went wrong. Try again";
    messengerService.sendTextMessage(patient.getIdFacebook(), message);
  }


  private int getPatient(Patient patient){
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();

    headers.set("Authorization", "Bearer " + accessToken);
    String url = defaultUrl + "/api/patients";
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("first_name", patient.getFirstName())
            .queryParam("last_name", patient.getLastName())
            .queryParam("chart_id", patient.getChartId());
    HttpEntity entity = new HttpEntity(headers);
    ResponseEntity<String> response = restTemplate.exchange(
            builder.toUriString(), HttpMethod.GET, entity, String.class);
    if (response.getStatusCode().value() == 200) {
      JSONObject jsonObject = new JSONObject(response.getBody());
      JSONArray array = jsonObject.getJSONArray("results");
      JSONObject patientJson = (JSONObject) array.get(0);
      String id = patientJson.get("chart_id").toString();
      patient.setChartId(id);
      // patientService.save(patient);
    }
    return response.getStatusCode().value();
  }
}