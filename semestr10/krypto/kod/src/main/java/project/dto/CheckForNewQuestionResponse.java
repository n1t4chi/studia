package project.dto;

import lombok.*;
import project.Group;

@Data
public class CheckForNewQuestionResponse {
    private final String question;
    private final Group group;
    private final int index;
    private final int participants;
}
