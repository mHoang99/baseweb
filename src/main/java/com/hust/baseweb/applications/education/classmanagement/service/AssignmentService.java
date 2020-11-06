package com.hust.baseweb.applications.education.classmanagement.service;

import com.hust.baseweb.applications.education.exception.SimpleResponse;
import com.hust.baseweb.applications.education.model.CreateAssignmentIM;
import com.hust.baseweb.applications.education.model.getassignmentdetail.GetAssignmentDetailOM;
import com.hust.baseweb.applications.education.model.getassignmentdetail4teacher.GetAssignmentDetail4TeacherOM;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface AssignmentService {

    GetAssignmentDetailOM getAssignmentDetail(UUID id, String studentId);

    GetAssignmentDetail4TeacherOM getAssignmentDetail4Teacher(UUID assignmentId);

    String getSubmissionsOf(String assignmentId, List<String> studentIds);

    SimpleResponse deleteAssignment(UUID id);

    SimpleResponse createAssignment(CreateAssignmentIM im);

    SimpleResponse updateAssignment(UUID id, CreateAssignmentIM im);

    SimpleResponse saveSubmission(String studentId, UUID assignmentId, MultipartFile file);
}
