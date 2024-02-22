package backend.models.request;

import lombok.*;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {
    private String name;
    private String password;
}