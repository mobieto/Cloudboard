package com.whiteboard.whiteboardapp2.Repo;

import com.whiteboard.whiteboardapp2.Model.WhiteboardAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhiteboardActionRepository extends JpaRepository<WhiteboardAction, Long> {
    WhiteboardAction findByCoords(String coords);
}
