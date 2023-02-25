package edu.wpi.FlashyFrogs.Alerts;

import static edu.wpi.FlashyFrogs.DBConnection.CONNECTION;

import edu.wpi.FlashyFrogs.Accounts.CurrentUserEntity;
import edu.wpi.FlashyFrogs.ORM.Alert;
import edu.wpi.FlashyFrogs.ORM.Department;
import jakarta.persistence.RollbackException;
import java.sql.Date;
import java.time.ZoneId;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Setter;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class NewAlertController {
  @Setter private PopOver popOver;
  @Setter private AlertManagerController alertManagerController;
  @FXML private TextField summaryField;
  @FXML private TextArea descriptionField;
  @FXML private SearchableComboBox<Department> deptBox;
  @FXML private ComboBox<Alert.Severity> severityBox;
  @FXML private DatePicker date;

  public void initialize() {
    Session session = CONNECTION.getSessionFactory().openSession();
    List<Department> departments =
        session.createQuery("FROM Department", Department.class).getResultList();

    deptBox.setItems(FXCollections.observableArrayList(departments));
    severityBox.setItems(FXCollections.observableArrayList(Alert.Severity.values()));
  }

  public void handleSubmit(javafx.event.ActionEvent actionEvent) {
    Session session = CONNECTION.getSessionFactory().openSession();
    Transaction transaction = session.beginTransaction();

    try {
      if (summaryField.getText().equals("") || descriptionField.getText().equals("") || date.getValue().toString().equals("")) {
        throw new NullPointerException();
      }

      Alert alert =
          new Alert(
              Date.from(date.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()),
              CurrentUserEntity.CURRENT_USER.getCurrentUser(),
              summaryField.getText(),
              descriptionField.getText(),
              deptBox.getValue(),
              severityBox.getValue());

      try {
        session.persist(alert);
        transaction.commit();
        session.close();
        alertManagerController.initialize();
        popOver.hide();
        //        handleCancel(actionEvent);
      } catch (RollbackException exception) {
        session.clear();
        // TODO Do something smart and throw an error maybe
        session.close();
      } catch (Exception exception) {
        session.clear();
        // TODO Do something smart also
        session.close();
      }
    } catch (ArrayIndexOutOfBoundsException | NullPointerException exception) {
      session.clear();
      // TODO Do something smart and throw an error maybe
      session.close();
    }
  }
}
