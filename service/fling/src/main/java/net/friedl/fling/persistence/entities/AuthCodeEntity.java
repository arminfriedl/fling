package net.friedl.fling.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "AuthCode")
@Getter @Setter
public class AuthCodeEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String authCode;

    @ManyToOne(optional = false)
    private FlingEntity fling;

    public void setFling(FlingEntity fling) {
        this.fling = fling;
        fling.getAuthCodes().add(this);
    }
}
