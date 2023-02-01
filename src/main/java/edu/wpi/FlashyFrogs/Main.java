package edu.wpi.FlashyFrogs;

import edu.wpi.FlashyFrogs.ORM.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class Main {

  static final StandardServiceRegistry registry =
      new StandardServiceRegistryBuilder()
          .configure("edu/wpi/FlashyFrogs/hibernate.cfg.xml") // Load settings
          .build();

  public static SessionFactory factory =
      new MetadataSources(registry).buildMetadata().buildSessionFactory();

  public static void main(String[] args) throws FileNotFoundException {
    File nodeFile = new File("ssrc/main/resources/CSVFiles/L1Nodes.csv");
    File edgeFile = new File("src/main/resources/CSVFiles/L1Edges.csv");
    File moveFile = new File("src/main/resources/CSVFiles/move.csv");
    File locationFile = new File("src/main/resources/CSVFiles/locationName.csv");

    CSVParser.readFiles(nodeFile, edgeFile, locationFile, moveFile, factory);



    //    Fapp.launch(Fapp.class, args);
    factory.close();
    registry.close();
  }
}
