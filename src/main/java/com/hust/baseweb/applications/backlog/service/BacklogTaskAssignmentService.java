package com.hust.baseweb.applications.backlog.service;

import com.hust.baseweb.applications.backlog.entity.BacklogTaskAssignment;
import com.hust.baseweb.applications.backlog.model.CreateBacklogTaskAssignmentInputModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public interface BacklogTaskAssignmentService {
    List<BacklogTaskAssignment> save(CreateBacklogTaskAssignmentInputModel input);
    List<BacklogTaskAssignment> findAllByBacklogTaskId(UUID backlogTaskId);
    List<BacklogTaskAssignment> findAllActiveByBacklogTaskId(UUID backlogTaskId);
}
