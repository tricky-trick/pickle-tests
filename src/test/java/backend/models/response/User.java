package backend.models.response;

import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class User {
    private String name;
    private String userId;
}