package edu.wpi.FlashyFrogs.controllers;

import edu.wpi.FlashyFrogs.Fapp;
import edu.wpi.FlashyFrogs.ORM.AudioVisual;
import edu.wpi.FlashyFrogs.ORM.InternalTransport;
import edu.wpi.FlashyFrogs.ORM.LocationName;
import edu.wpi.FlashyFrogs.ORM.ServiceRequest;
import io.github.palexdev.materialfx.controls.MFXButton;
import java.io.IOException;
import java.sql.Connection;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.persistence.RollbackException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.controlsfx.control.SearchableComboBox;
import org.hibernate.Session;
import org.hibernate.Transaction;

import static edu.wpi.FlashyFrogs.DBConnection.CONNECTION;

public class HoldAVController {

  @FXML MFXButton clear;
  @FXML MFXButton submit;
  @FXML MFXButton credits;
  @FXML MFXButton back;
  @FXML MFXButton AV;
  @FXML MFXButton IT;
  @FXML MFXButton IPT;
  @FXML MFXButton sanitation;
  @FXML MFXButton security;
  @FXML SearchableComboBox location;
  @FXML SearchableComboBox type;
  @FXML TextField device;
  @FXML TextField model;
  @FXML TextField reason;
  @FXML DatePicker date;
  @FXML TextField time;
  @FXML SearchableComboBox urgency;
  @FXML TextField description;

  @FXML Text h1;
  @FXML Text h2;
  @FXML Text h3;
  @FXML Text h4;
  @FXML Text h5;
  @FXML Text h6;
  @FXML Text h7;
  @FXML Text h8;
  @FXML Text h9;

  @FXML private Label errorMessage;

  boolean hDone = false;
  private Connection connection = null;

  public void initialize() {
    h1.setVisible(false);
    h2.setVisible(false);
    h3.setVisible(false);
    h4.setVisible(false);
    h5.setVisible(false);
    h6.setVisible(false);
    h7.setVisible(false);
    h8.setVisible(false);
    h9.setVisible(false);

    Session session = CONNECTION.getSessionFactory().openSession();
    List<String> objects =
            session.createQuery("SELECT longName FROM LocationName", String.class).getResultList();

    objects.sort(String::compareTo);

    ObservableList<String> observableList = FXCollections.observableList(objects);


    location.setItems(observableList);
    type.getItems()
        .addAll(
            "Lobby", "Waiting Room", "Patient Room", "Hallway", "Stairway", "Elevator", "Other");
    urgency.getItems().addAll("Very Urgent", "Moderately Urgent", "Not Urgent");
  }

  public void handleSubmit(ActionEvent actionEvent) throws IOException {
    Session session = CONNECTION.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    try {
      String urgencyString = urgency.getValue().toString().toUpperCase().replace(" ", "_");
      String timeString = time.getText().toUpperCase().replace(" ", "_");

      // check
      if (location.getValue().toString().equals("")
              || type.getValue().toString().equals("")
              || device.getText().equals("")
              || model.getText().equals("")
              || reason.getText().equals("")
              || date.getValue().toString().equals("")
              || description.getText().equals("")) {
        throw new NullPointerException();
      }

      Date dateNeeded =
              Date.from(
                      date
                              .getValue()
                              .atStartOfDay(ZoneId.systemDefault())
                              .toInstant());

      AudioVisual audioVisual = new AudioVisual();
      //this needs to be updated when database is fixed
      /*audioVisual.setLocation(session.find(LocationName.class, location.getValue().toString()));
      audioVisual.setLocationType(type.getValue().toString());
      audioVisual.setDeviceType(device.getText());
      audioVisual.setDeviceModel(model.getText());
      audioVisual.setReason(reason.getText());
      audioVisual.setDateOfIncident(dateNeeded);
      audioVisual.setTime(time.getText());
      audioVisual.setUrgency(ServiceRequest.Urgency.valueOf(urgencyString));
      audioVisual.setDescription(reason.getText());*/
      try {
        session.persist(audioVisual);
        transaction.commit();
        session.close();
        handleClear(actionEvent);
        errorMessage.setTextFill(javafx.scene.paint.Paint.valueOf("#012D5A"));
        errorMessage.setText("Successfully submitted.");
      } catch (RollbackException exception) {
        session.clear();
        errorMessage.setTextFill(javafx.scene.paint.Paint.valueOf("#b6000b"));
        errorMessage.setText("Please fill all fields.");
        session.close();
      }
    } catch (ArrayIndexOutOfBoundsException | NullPointerException exception) {
      session.clear();
      errorMessage.setTextFill(Paint.valueOf("#b6000b"));
      errorMessage.setText("Please fill all fields.");
      session.close();
    }
  }

  public void handleClear(ActionEvent actionEvent) throws IOException {
    location.valueProperty().set(null);
    type.valueProperty().set(null);
    device.setText("");
    model.setText("");
    date.valueProperty().set(null);
    time.setText("");
    urgency.valueProperty().set(null);
    description.setText("");
  }

  public void help() {
    if (hDone = false) {
      h1.setVisible(true);
      h2.setVisible(true);
      h3.setVisible(true);
      h4.setVisible(true);
      h5.setVisible(true);
      h6.setVisible(true);
      h7.setVisible(true);
      h8.setVisible(true);
      h9.setVisible(true);
      hDone = true;
    }
    if (hDone = true) {
      h1.setVisible(false);
      h2.setVisible(false);
      h3.setVisible(false);
      h4.setVisible(false);
      h5.setVisible(false);
      h6.setVisible(false);
      h7.setVisible(false);
      h8.setVisible(false);
      h9.setVisible(false);
      hDone = false;
    }
  }

  public void handleAV(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "AudioVisualService");
  }

  public void handleIT(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "ITService");
  }

  public void handleIPT(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "TransportService");
  }

  public void handleSanitation(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "SanitationService");
  }

  public void handleSecurity(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "SecurityService");
  }

  public void handleCredits(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "Credits");
  }

  public void handleBack(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("views", "Home");
  }
}
