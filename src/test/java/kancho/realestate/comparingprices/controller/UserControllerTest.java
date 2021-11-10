package kancho.realestate.comparingprices.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;

import javax.naming.AuthenticationException;
import javax.servlet.http.Cookie;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;

import kancho.realestate.comparingprices.exception.DuplicateLoginException;
import kancho.realestate.comparingprices.exception.DuplicateUserAccountException;
import kancho.realestate.comparingprices.exception.IdNotExistedException;
import kancho.realestate.comparingprices.exception.PasswordWrongException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserController userController;

	@Test
	@Transactional
	public void join_회원가입_성공() throws Exception {
		String serializedUser = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser, status().isCreated());
	}

	@Test
	@Transactional
	public void join_회원가입_중복_오류() throws Exception {
		String serializedUser = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser, status().isCreated());
		postExceptionTest("/join", serializedUser, status().isBadRequest(),
			result -> assertThat(result.getResolvedException()).isInstanceOf(DuplicateUserAccountException.class));
	}

	@Test
	@Transactional
	public void login_로그인_성공() throws Exception {
		String serializedUser = serailizedTesterBody("tom ford", "12345678");

		postSuccessTest("/join", serializedUser, status().isCreated());
		postSuccessTest("/login", serializedUser, status().isCreated());
	}

	@Test
	@Transactional
	public void login_로그인_없는계정_오류() throws Exception {
		String serializedUser1 = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser1, status().isCreated());

		String serializedUser2 = serailizedTesterBody("tom ford9", "12345678");
		postExceptionTest("/login", serializedUser2, status().isBadRequest(),
			result -> assertThat(result.getResolvedException()).isInstanceOf(IdNotExistedException.class));
	}

	@Test
	@Transactional
	public void login_로그인_비밀번호_오류() throws Exception {
		String serializedUser1 = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser1, status().isCreated());

		String serializedInvalidPasswordUser = serailizedTesterBody("tom ford", "a232fds");
		postExceptionTest("/login", serializedInvalidPasswordUser, status().isBadRequest(),
			result -> assertThat(result.getResolvedException()).isInstanceOf(PasswordWrongException.class));
	}

	@Test
	@Transactional
	public void login_로그인한_사람이_같은_아이디로_로그인요청() throws Exception {
		String serializedUser1 = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser1, status().isCreated());
		MvcResult loginResult = postSuccessTest("/login", serializedUser1, status().isCreated());

		postExceptionTest("/login", serializedUser1, status().isBadRequest(),
			result -> assertThat(result.getResolvedException()).isInstanceOf(DuplicateLoginException.class),
			loginResult.getResponse().getCookie("SESSION"));
	}

	@Test
	@Transactional
	public void login_로그인한_사람이_다른_아이디로_로그인요청() throws Exception {
		String serializedUser1 = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser1, status().isCreated());

		String serializedUser2 = serailizedTesterBody("adam smith", "asd23121238");
		postSuccessTest("/join", serializedUser2, status().isCreated());

		MvcResult loginResult = postSuccessTest("/login", serializedUser1, status().isCreated());
		Cookie user1Cookie = loginResult.getResponse().getCookie("SESSION");

		MvcResult loginWithOtherAccountResult = postSuccessTest("/login", serializedUser2, status().isCreated(),
			user1Cookie);
		Cookie user2Cookie = loginWithOtherAccountResult.getResponse().getCookie("SESSION");

		// 세션키 바뀜 검증
		assertThat(user1Cookie).isNotEqualTo(user2Cookie);

		getExceptionTest("/my-estate/test", status().isBadRequest(),
			result -> assertThat(result.getResolvedException()).isInstanceOf(AuthenticationException.class),
			user1Cookie);
	}

	@Test
	@Transactional
	public void join_login_로그인_상태에서_회원가입_요청_예외처리() throws Exception {
		String serializedUser1 = serailizedTesterBody("tom ford", "12345678");
		postSuccessTest("/join", serializedUser1, status().isCreated());

		MvcResult loginResult = postSuccessTest("/login", serializedUser1, status().isCreated());
		Cookie user1Cookie = loginResult.getResponse().getCookie("SESSION");

		String serializedUser2 = serailizedTesterBody("adam smith", "asd23121238");
		postExceptionTest("/join", serializedUser2, status().isBadRequest(),
			result -> assertThat(result.getResolvedException()).isInstanceOf(IllegalStateException.class),user1Cookie);
	}

	public MvcResult postSuccessTest(String path, String content, ResultMatcher resultMatcher) throws Exception {
		return mockMvc.perform(post(path)
			.content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(resultMatcher)
			.andReturn();
	}

	public MvcResult postSuccessTest(String path, String content, ResultMatcher resultMatcher, Cookie cookie) throws
		Exception {
		return mockMvc.perform(post(path)
			.content(content)
			.cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(resultMatcher)
			.andReturn();
	}

	public void postExceptionTest(String path, String content, ResultMatcher resultStatus,
		ResultMatcher resultException) throws Exception {
		mockMvc.perform(post(path)
			.content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(resultStatus)
			.andExpect(resultException);
	}

	public void postExceptionTest(String path, String content, ResultMatcher resultStatus,
		ResultMatcher resultException, Cookie cookie) throws Exception {
		mockMvc.perform(post(path)
			.content(content)
			.cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(resultStatus)
			.andExpect(resultException);
	}

	public void getExceptionTest(String path, ResultMatcher resultStatus,
		ResultMatcher resultException, Cookie cookie) throws Exception {
		mockMvc.perform(get(path)
			.cookie(cookie)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(resultStatus)
			.andExpect(resultException);
	}

	public String serailizedTesterBody(String id, String password) {
		HashMap<String, String> bodyContent = new HashMap<>();
		bodyContent.put("id", id);
		bodyContent.put("password", password);
		return String.valueOf(new JSONObject(bodyContent));
	}

}