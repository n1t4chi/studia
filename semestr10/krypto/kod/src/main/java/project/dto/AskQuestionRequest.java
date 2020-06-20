package project.dto;

import lombok.*;

@Data
public class AskQuestionRequest {
    @NonNull
    private final String id;
    @NonNull
    private final String question;
}
