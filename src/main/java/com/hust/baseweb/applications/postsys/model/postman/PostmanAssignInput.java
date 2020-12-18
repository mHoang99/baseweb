package com.hust.baseweb.applications.postsys.model.postman;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostmanAssignInput {
    String postmanId;
    List<String> postOrderIds;
}
