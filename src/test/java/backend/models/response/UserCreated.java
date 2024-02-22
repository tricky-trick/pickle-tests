package backend.models.response;

import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class UserCreated {
    private String userID;
    private String name;
    private String[] books;
}