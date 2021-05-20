package com.hust.baseweb.applications.education.quiztest.repo;

import java.util.List;

import com.hust.baseweb.applications.education.quiztest.entity.EduQuizTest;
import com.hust.baseweb.applications.education.quiztest.model.StudentInTestQueryReturnModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EduQuizTestRepo extends JpaRepository<EduQuizTest, String>{
    @Query(
        nativeQuery = true, 
        value = "select * from edu_quiz_test where created_by_user_login_id = ?1"
    )
    public List<EduQuizTest> findByCreateUser(String userLoginId);


    public static interface StudentInfo {

        String getTest_id();

        String getUser_login_id();

        String getFull_name();

        String getGender();

        String getBirth_date();

        String getEmail();
   
    }

    @Query(
        nativeQuery = true,
        value = 
            "select \n" + 
            "S1.test_id, \n" + 
            "S1.participant_user_login_id as user_login_id, \n" + 
            "person.first_name || ' ' || person.middle_name || ' ' || person.last_name as full_name, \n" + 
            "person.gender, \n" + 
            "person.birth_date, \n" + 
            "user_register.email \n" + 
            "from edu_test_quiz_participant S1 \n" + 
            "inner join user_login \n" + 
            "on S1.participant_user_login_id = user_login.user_login_id \n" + 
            "inner join person \n" + 
            "on person.party_id = user_login.party_id \n" + 
            "left join user_register \n"+ 
            "on user_register.user_login_id = user_login.user_login_id \n" + 
            "where S1.test_id = ?1"
    )
    public List<StudentInfo> findAllStudentInTest(String testId);
}
