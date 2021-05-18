package com.hust.baseweb.applications.education.quiztest.entity.compositeid;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class CompositeQuizGroupQuestionParticipationExecutionChoiceId implements Serializable {
    private UUID questionId;
    private UUID quizGroupId;
    private String participationUserLoginId;
    private UUID choiceAnswerId;

}
