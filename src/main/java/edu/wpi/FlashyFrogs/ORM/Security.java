package edu.wpi.FlashyFrogs.ORM;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

@Entity
@Table(name = "Security")
public class Security {
  @Basic @Getter @Setter String incidentReport;
  @Basic @Getter @Setter String location;

  @Basic @Id @Getter @Setter BigInteger srID;
}
