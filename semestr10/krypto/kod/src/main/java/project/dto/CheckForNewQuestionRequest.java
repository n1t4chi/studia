package project.dto;

import lombok.*;

@Data
public class CheckForNewQuestionRequest {
    @NonNull
    private final String id;
}
