package com.whiteboard.whiteboardapp2.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class WhiteboardAction implements Serializable {
    @Id
    private String id;

    private Long x;

    private Long y;

    private String action; // "stroke,1,black,-1,-1" , "text,'Hello world!'" , "shape,square,10,10"

    public WhiteboardAction() {
        this.id = Long.toString(System.currentTimeMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) { this.id = id; }

    public Long getX() {
        return x;
    }

    public void setX(Long x) {
        this.x = x;
    }

    public Long getY() {
        return y;
    }

    public void setY(Long y) {
        this.y = y;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
