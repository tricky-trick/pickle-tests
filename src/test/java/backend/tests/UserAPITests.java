package backend.tests;

import backend.UserAPIClient;
import builder.HeadersBuilder;
import builder.OkhttpClientBuilder;
import builder.RetrofitBuilder;
import com.github.javafaker.Faker;
import factory.RetrofitClientFactory;
import backend.models.request.User;
import backend.models.response.UserCreated;
import okhttp3.OkHttpClient;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UserAPITests {


    private static UserAPIClient userAPIClient;

    @BeforeClass
    public static void setUp() {
        OkHttpClient okHttpClient = OkhttpClientBuilder.builder().addHeaders(HeadersBuilder.builder().setHeaders("Content-Type", "application/json").build()).build();
        Retrofit retrofit = RetrofitBuilder.builder().setBaseUrl("https://bookstore.toolsqa.com/").setCallAdapterFactory("gson").client(okHttpClient).build();
        userAPIClient = RetrofitClientFactory.createApiClient(retrofit, UserAPIClient.class);
    }

    @Test
    public void testCreateUser() {
        Faker faker = new Faker();
        final String name = faker.name().username();
        final String password = faker.internet().password();
        final String userID = null;
        Response<UserCreated> userCreatedResponse = RetrofitClientFactory.executeCall(userAPIClient.createUser(User.builder().name(name).password(password).build()));
        System.out.println(userCreatedResponse.body().toString());
        Assert.assertEquals(String.valueOf(userCreatedResponse.code()), 201, "Status Code");
        Assert.assertEquals(userCreatedResponse.body().getName(), name, "User Name");
        Assert.assertEquals(userCreatedResponse.body().getUserID(), userID, "User ID");
    }
}