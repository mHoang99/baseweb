package com.hust.baseweb.applications.education.quiztest.controller;

import com.hust.baseweb.applications.education.classmanagement.service.ClassService;
import com.hust.baseweb.applications.education.quiztest.entity.EduQuizTest;
import com.hust.baseweb.applications.education.quiztest.entity.HistoryLogQuizGroupQuestionParticipationExecutionChoice;
import com.hust.baseweb.applications.education.quiztest.entity.QuizGroupQuestionParticipationExecutionChoice;
import com.hust.baseweb.applications.education.quiztest.model.HistoryLogQuizGroupQuestionParticipationExecutionChoiceDetailModel;
import com.hust.baseweb.applications.education.quiztest.model.QuizGroupQuestionParticipationExecutionChoiceInputModel;
import com.hust.baseweb.applications.education.quiztest.repo.EduQuizTestRepo;
import com.hust.baseweb.applications.education.quiztest.repo.HistoryLogQuizGroupQuestionParticipationExecutionChoiceRepo;
import com.hust.baseweb.applications.education.quiztest.repo.QuizGroupQuestionParticipationExecutionChoiceRepo;
import com.hust.baseweb.applications.education.service.CourseService;
import com.hust.baseweb.service.PersonService;
import com.hust.baseweb.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Log4j2
@Controller
@Validated
@AllArgsConstructor(onConstructor = @__(@Autowired))
@CrossOrigin

public class QuizGroupQuestionParticipationExecutionChoiceController {

    QuizGroupQuestionParticipationExecutionChoiceRepo quizGroupQuestionParticipationExecutionChoiceRepo;
    EduQuizTestRepo eduQuizTestRepo;
    HistoryLogQuizGroupQuestionParticipationExecutionChoiceRepo historyLogQuizGroupQuestionParticipationExecutionChoiceRepo;
    UserService userService;
    PersonService personService;
    ClassService classService;
    CourseService courseService;

    @PostMapping("/quiz-test-choose_answer-by-user")
    public ResponseEntity<?> quizChooseAnswer(
        Principal principal,
        @RequestBody @Valid QuizGroupQuestionParticipationExecutionChoiceInputModel input
    ) {
        EduQuizTest test = eduQuizTestRepo.findById(input.getTestId()).get();
        Date currentDate = new Date();
        Date testStartDate = test.getScheduleDatetime();
        int timeTest = ((int) (currentDate.getTime() - testStartDate.getTime()))/(60*1000); //minutes
        //System.out.println(currentDate);
        //System.out.println(testStartDate);
        //System.out.println(timeTest);
        //System.out.println(test.getDuration());

        if(timeTest > test.getDuration()){
            //System.out.println("out time~!");
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(null);
        }
        UUID questionId = input.getQuestionId();
        UUID groupId = input.getQuizGroupId();
        String userId = principal.getName();
        List<UUID> chooseAnsIds = input.getChooseAnsIds();

        if(chooseAnsIds == null){
            log.info("quizChooseAnswer, chooseAnsIds = null");
        }else {
            log.info("quizChooseAnswer, chooseAnsIds = " + chooseAnsIds.size());
        }

        List<QuizGroupQuestionParticipationExecutionChoice> a =  quizGroupQuestionParticipationExecutionChoiceRepo.findQuizGroupQuestionParticipationExecutionChoicesByParticipationUserLoginIdAndQuizGroupIdAndQuestionId(userId,groupId,questionId);
        a.forEach(quizGroupQuestionParticipationExecutionChoice -> {
            quizGroupQuestionParticipationExecutionChoiceRepo.delete(quizGroupQuestionParticipationExecutionChoice);
            log.info("quizChooseAnswer, chooseAnsIds, delete previous choice answer for question " + questionId + " of groupId " + groupId + " of user " + userId);
        });

        Date createdStamp = new Date();
        for (UUID choiceId:
             chooseAnsIds) {
            QuizGroupQuestionParticipationExecutionChoice tmp = new QuizGroupQuestionParticipationExecutionChoice();
            tmp.setQuestionId(questionId);
            tmp.setQuizGroupId(groupId);
            tmp.setParticipationUserLoginId(userId);
            tmp.setChoiceAnswerId(choiceId);
            quizGroupQuestionParticipationExecutionChoiceRepo.save(tmp);


            // create history log
            HistoryLogQuizGroupQuestionParticipationExecutionChoice historyLogQuizGroupQuestionParticipationExecutionChoice
                = new HistoryLogQuizGroupQuestionParticipationExecutionChoice();
            historyLogQuizGroupQuestionParticipationExecutionChoice.setChoiceAnswerId(choiceId);
            historyLogQuizGroupQuestionParticipationExecutionChoice.setParticipationUserLoginId(userId);
            historyLogQuizGroupQuestionParticipationExecutionChoice.setQuestionId(questionId);
            historyLogQuizGroupQuestionParticipationExecutionChoice.setQuizGroupId(groupId);
            historyLogQuizGroupQuestionParticipationExecutionChoice.setCreatedStamp(createdStamp);
            historyLogQuizGroupQuestionParticipationExecutionChoice = historyLogQuizGroupQuestionParticipationExecutionChoiceRepo
                .save(historyLogQuizGroupQuestionParticipationExecutionChoice);
        }



        return ResponseEntity.ok().body(chooseAnsIds);
    }

    @GetMapping("/get-history-log-quiz_group_question_participation_execution_choice/{testId}")
    public ResponseEntity<?> getHistoryLogQuizGroupQuestionParticipationExecutionChoice(Principal principal, @PathVariable String testId){
        log.info("getHistoryLogQuizGroupQuestionParticipationExecutionChoice, testId = " + testId);

        List<HistoryLogQuizGroupQuestionParticipationExecutionChoice> list =
            historyLogQuizGroupQuestionParticipationExecutionChoiceRepo.findAll();
        List<HistoryLogQuizGroupQuestionParticipationExecutionChoiceDetailModel> modelList
            = new ArrayList();
        DateFormat formetter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        for(HistoryLogQuizGroupQuestionParticipationExecutionChoice e: list){
            HistoryLogQuizGroupQuestionParticipationExecutionChoiceDetailModel m =
                new HistoryLogQuizGroupQuestionParticipationExecutionChoiceDetailModel();
            m.setChoiceAnswerId(e.getChoiceAnswerId());
            String sDate = "";
            if(e.getCreatedStamp() != null){
                try{
                    sDate = formetter.format(e.getCreatedStamp());
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
            m.setDate(sDate);
            m.setUserLoginId(e.getParticipationUserLoginId());
            m.setQuestionId(e.getQuestionId());
            m.setQuizGroupId(e.getQuizGroupId());
            modelList.add(m);
        }
        return ResponseEntity.ok().body(modelList);
    }
}
