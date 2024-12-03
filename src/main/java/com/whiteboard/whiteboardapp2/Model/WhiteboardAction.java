package com.whiteboard.whiteboardapp2.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class WhiteboardAction {
    @Id
    @GeneratedValue
    private Long id;

    private String coords;

    private String action; // "action:stroke,width:1,colour:black" , "action:text,text:'Hello world!'" , "action:shape,shape:square,width:10,height:10"

    public Long getId() {
        return id;
    }

    public void setId(Long id) { this.id = id; }

    public String getCoords() { return coords; }

    public void setCoords(String coords) {
        this.coords = coords;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
