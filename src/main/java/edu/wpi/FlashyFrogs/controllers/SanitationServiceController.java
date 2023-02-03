package edu.wpi.FlashyFrogs.controllers;

import static edu.wpi.FlashyFrogs.Main.factory;

import edu.wpi.FlashyFrogs.Fapp;
import edu.wpi.FlashyFrogs.ORM.LocationName;
import edu.wpi.FlashyFrogs.ORM.Sanitation;
import edu.wpi.FlashyFrogs.ORM.ServiceRequest;
import edu.wpi.FlashyFrogs.SanitationServiceData;
import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXComboBox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXTextField;
import java.io.IOException;
import java.sql.Connection;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SanitationServiceController extends ServiceRequestController {
  @FXML MFXButton clearButton; // fx:ID of the button in the ExampleFXML
  @FXML MFXButton submitButton;
  @FXML MFXButton backButton;
  @FXML MFXComboBox requestTypeDropDown;
  @FXML MFXComboBox locationDropDown;
  @FXML MFXDatePicker date;
  @FXML MFXTextField firstName;
  @FXML MFXTextField lastName;
  @FXML MFXTextField middleName;
  @FXML MFXTextField timeEntry;
  @FXML MFXComboBox departmentDropDown;
  @FXML MFXComboBox urgencyEntry;
  @FXML private MFXTextField first2;
  @FXML private MFXTextField middle2;
  @FXML private MFXTextField last2;
  @FXML private MFXComboBox department2;
  @FXML private MFXButton allButton;
  private Connection connection = null; // connection to database
  private SanitationServiceData sanitationServiceData;

  /** Method run when controller is initializes */
  public void initialize() {

    sanitationServiceData = new SanitationServiceData();
    urgencyEntry.getItems().addAll("Very Urgent", "Moderately Urgent", "Not Urgent");
    requestTypeDropDown.getItems().addAll("Mopping", "Sweeping", "Vacuuming");
    // locationDropDown.getItems().addAll("room 1", "room 2", "public space 1", "public space 2");

    // I don't really know what to put here
    departmentDropDown.getItems().addAll("Nursing", "Cardiology", "Radiology", "Maintenance");

    Session session = factory.openSession();

    List<String> objects =
        session.createQuery("SELECT longName FROM LocationName", String.class).getResultList();
    session.close();
    locationDropDown.setItems(FXCollections.observableList(objects));

    department2.getItems().addAll("Nursing", "Cardiology", "Radiology", "Maintenance");
  }

  /**
   * clears all fields and drop downs
   *
   * @param actionEvent event that triggered method
   * @throws IOException
   */
  public void handleClear(ActionEvent actionEvent) throws IOException {
    requestTypeDropDown.clear();
    locationDropDown.clear();
    date.clear();
    firstName.clear();
    lastName.clear();
    middleName.clear();
    departmentDropDown.clear();
    timeEntry.clear();
    urgencyEntry.clear();
    first2.clear();
    middle2.clear();
    last2.clear();
    department2.clear();
  }

  /**
   * submits all fields by setting attributes of SubmitInfo instance
   *
   * @param actionEvent event that triggered method
   * @throws IOException
   */
  public void handleSubmit(ActionEvent actionEvent) throws IOException {

    Session session = factory.openSession();
    Transaction transaction = session.beginTransaction();

    String[] parts = {};
    String departmentEnumString = departmentDropDown.getText().toUpperCase();

    parts = urgencyEntry.getText().toUpperCase().split(" ");
    String urgencyEnumString = parts[0] + "_" + parts[1];
    // TODO: handle empty case and close session!!!!!

    String departmentEnumString2 = department2.getText().toUpperCase();

    String requestTypeEnumString = requestTypeDropDown.getText().toUpperCase();

    Sanitation sanitationRequest =
        new Sanitation(
            Sanitation.SanitationType.valueOf(requestTypeEnumString),
            firstName.getText(),
            middleName.getText(),
            lastName.getText(),
            first2.getText(),
            middle2.getText(),
            last2.getText(),
            ServiceRequest.EmpDept.valueOf(departmentEnumString),
            ServiceRequest.EmpDept.valueOf(departmentEnumString2),
            new Date(),
            Date.from(Instant.now()),
            ServiceRequest.Urgency.valueOf(urgencyEnumString),
            session.find(LocationName.class, locationDropDown.getText()));

    session.persist(sanitationRequest);
    transaction.commit();
    session.close();
    System.out.println(sanitationServiceData);
  }

  private void addSanitationRequest(SanitationServiceData sd) {
    Session session = factory.openSession();
    Transaction transaction = session.beginTransaction();
    Sanitation sanitationRequest = new Sanitation();
    sanitationRequest.setLocation(null);
    sanitationRequest.setType(Sanitation.SanitationType.valueOf(sd.getRequestType()));
    sanitationRequest.setEmpFirstName(sd.getEmployeeFirstName());
    sanitationRequest.setEmpMiddleName(sd.getEmployeeMiddleName());
    sanitationRequest.setEmpLastName(sd.getEmployeeLastName());
    sanitationRequest.setDateOfSubmission(Date.from(Instant.now()));

    session.persist(sanitationRequest);
    transaction.commit();
    session.close();
    System.out.println("submitted");
  }

  /**
   * loads another scene
   *
   * @param actionEvent event that triggered method
   * @throws IOException
   */
  public void handleBack(ActionEvent actionEvent) throws IOException {
    Fapp.setScene("RequestsHome");
  }

  @FXML
  public void handleAllButton(ActionEvent actionEvent) throws IOException {

    Fapp.setScene("AllSanitationRequest");
  }

  /**
   * generates a table to store button click information
   *
   * @return true when table is successfully created or already exists, false otherwise
   */
  //  private boolean createTable() {
  //
  //    boolean table_exists = false;
  //
  //    if (this.connection != null) {
  //      String createQuery =
  //          "CREATE TABLE APP.buttonClicks("
  //              + "id INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY
  // 1), "
  //              + "btn_name VARCHAR(50), "
  //              + "time_stamp TIMESTAMP NOT NULL, "
  //              + "PRIMARY KEY(id) )";
  //      try {
  //        Statement statement = this.connection.createStatement();
  //        statement.execute(createQuery);
  //
  //        table_exists = true;
  //      } catch (SQLException e) {
  //        // Error code 955 is "name is already used by an existing object", so this table name
  //        // already exists
  //        if (e.getErrorCode() == 955 || e.getMessage().contains("already exists"))
  //          table_exists = true;
  //        else e.printStackTrace();
  //      }
  //    }
  //    return table_exists;
  //  }
  //
  //  /**
  //   * Stores button click data to database
  //   *
  //   * @return true if data is stored successfully, false otherwise
  //   */
  //  private boolean logData() {
  //    if (connection != null) {
  //      String writeQuery =
  //          "INSERT INTO APP.buttonClicks(btn_name, time_stamp) VALUES ( 'ClickButton',
  // CURRENT_TIMESTAMP ) ";
  //      try {
  //        Statement statement = this.connection.createStatement();
  //        statement.execute(writeQuery);
  //        return true;
  //      } catch (SQLException e) {
  //        e.printStackTrace();
  //      }
  //    }
  //    return false;
  //  }
}