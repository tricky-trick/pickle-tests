package backend;

import backend.models.request.User;
import backend.models.response.UserCreated;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserAPIClient {

    @GET("Account/v1/User/{id}")
    Call<User> getUsersById(@Path("id") String id);

    @POST("Account/v1/User")
    Call<UserCreated> createUser(@Body User user);
}