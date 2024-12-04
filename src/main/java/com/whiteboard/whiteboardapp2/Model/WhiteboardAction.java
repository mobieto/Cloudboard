package com.whiteboard.whiteboardapp2.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class WhiteboardAction {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private Long coordX;
    private Long coordY;

    private String action; // "stroke,1,black,-1,-1" , "text,'Hello world!'" , "shape,square,10,10"

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public Long getCoordX() {
        return coordX;
    }

    public void setCoordX(Long coordX) {
        this.coordX = coordX;
    }

    public Long getCoordY() {
        return coordY;
    }

    public void setCoordY(Long coordY) {
        this.coordY = coordY;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
