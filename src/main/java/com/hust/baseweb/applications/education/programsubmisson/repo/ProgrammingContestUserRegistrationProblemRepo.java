package com.hust.baseweb.applications.education.programsubmisson.repo;

import com.hust.baseweb.applications.education.programsubmisson.entity.CompositeProgrammingContestUserRegistrationProblemId;
import com.hust.baseweb.applications.education.programsubmisson.entity.ProgrammingContestUserRegistrationProblem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgrammingContestUserRegistrationProblemRepo extends JpaRepository<ProgrammingContestUserRegistrationProblem, CompositeProgrammingContestUserRegistrationProblemId> {
    ProgrammingContestUserRegistrationProblem save(ProgrammingContestUserRegistrationProblem programmingContestUserRegistrationProblem);
    ProgrammingContestUserRegistrationProblem findByContestIdAndUserLoginIdAndProblemId(String contestId, String userLoginId, String problemId);
    List<ProgrammingContestUserRegistrationProblem> findByContestIdAndUserLoginId(String contestId, String userLoginId);

}
