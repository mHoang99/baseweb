package com.hust.baseweb.applications.education.service;

import com.hust.baseweb.applications.education.entity.QuizQuestion;
import com.hust.baseweb.applications.education.model.quiz.*;
import com.hust.baseweb.entity.UserLogin;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface QuizQuestionService {
    public QuizQuestion save(QuizQuestionCreateInputModel input);
    public QuizQuestion save(QuizQuestionCreateInputModel input, MultipartFile[] files);

    public List<QuizQuestion> findAll();
    public List<QuizQuestion> findQuizOfCourse(String courseId);
    public QuizQuestionDetailModel findQuizDetail(UUID questionId);
    public QuizQuestion changeOpenCloseStatus(UUID questionId);

    public boolean checkAnswer(UserLogin userLogin, QuizChooseAnswerInputModel quizChooseAnswerInputModel);

    public QuizQuestion findById(UUID questionId);

    public QuizQuestion update(UUID questionId, QuizQuestionUpdateInputModel input, MultipartFile[] files);
}
