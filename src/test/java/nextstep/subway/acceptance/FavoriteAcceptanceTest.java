package nextstep.subway.acceptance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static nextstep.subway.acceptance.AcceptanceUtils.응답코드_201을_반환한다;
import static nextstep.subway.acceptance.AcceptanceUtils.응답코드_400을_반환한다;
import static nextstep.subway.acceptance.AcceptanceUtils.응답코드_401을_반환한다;
import static nextstep.subway.acceptance.FavoriteSteps.인증없이_즐겨찾기_추가_요청;
import static nextstep.subway.acceptance.FavoriteSteps.즐겨찾기_목록_조회_요청하고_목록_반환;
import static nextstep.subway.acceptance.FavoriteSteps.즐겨찾기_추가_요청;
import static nextstep.subway.acceptance.MemberSteps.베어러_인증_로그인_요청하고_토큰_반환;
import static nextstep.subway.acceptance.StationSteps.지하철역_생성_요청;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class FavoriteAcceptanceTest extends AcceptanceTest {

    private static final String EMAIL = "admin@email.com";
    private static final String PASSWORD = "password";
    private static final String WRONG_TOKEN = "WRONG_TOKEN";

    private Long 강남역;
    private Long 양재역;
    private Long 정자역;

    private String token;

    /**
     * given: 지하철 역을 추가하고
     * given: 회원 가입을하고(super.setUp)
     * given: 로그인을 하고
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = 지하철역_생성_요청("강남역").jsonPath().getLong("id");
        양재역 = 지하철역_생성_요청("양재역").jsonPath().getLong("id");
        정자역 = 지하철역_생성_요청("정자역").jsonPath().getLong("id");

        token = 베어러_인증_로그인_요청하고_토큰_반환(EMAIL, PASSWORD);
    }

    /**
     * given: 올바른 로그인 토큰으로
     * when : 경로 즐겨찾기 추가 요청을 하면
     * then : 즐겨찾기 등록을 성공한다.
     */
    @DisplayName("올바른 인증 토큰으로 경로 즐겨찾기 추가 요청을 하면 성공적으로 즐겨찾기에 추가된다.")
    @Test
    void addFavorite() {
        final var response = 즐겨찾기_추가_요청(token, 강남역, 정자역);

        assertAll(
                () -> 응답코드_201을_반환한다(response),
                () -> assertThat(response.header(HttpHeaders.LOCATION)).isEqualTo("/favorites/1")
        );
    }

    /**
     * given: 올바른 로그인 토큰으로 경로 즐겨찾기 추가 요청을 하고
     * when : 즐겨찾기 목록을 조회하면
     * then : 추가한 경로를 확인할 수 있다.
     */
    @DisplayName("올바른 인증 토큰으로 경로 즐겨찾기 추가 요청을 하면 즐겨찾기 목록에서 해당 구간을 확인할 수 있다.")
    @Test
    void showFavorites() {
        // given
        즐겨찾기_추가_요청(token, 강남역, 정자역);

        // when
        final var 즐겨찾기_목록 = 즐겨찾기_목록_조회_요청하고_목록_반환(token);
        final var 추가된_즐겨찾기_경로 = 즐겨찾기_목록.get(0);

        // then
        assertAll(
                () -> assertThat(즐겨찾기_목록.size()).isEqualTo(1),
                () -> assertThat(추가된_즐겨찾기_경로.getSource().getId()).isEqualTo(강남역),
                () -> assertThat(추가된_즐겨찾기_경로.getTarget().getId()).isEqualTo(정자역)
        );
    }

    /**
     * when : 인증 없이 경로 즐겨찾기 추가 요청을 하면
     * then : 오류가 발생한다.
     */
    @DisplayName("인증 없이 경로 즐겨찾기 추가 요청을 하면 오류가 발생한다.")
    @Test
    void addFavoriteWithNoAuth() {
        // when
        final var response = 인증없이_즐겨찾기_추가_요청(강남역, 정자역);

        // then
        응답코드_401을_반환한다(response);
    }

    /**
     * given: 올바르지 않은 로그인 토큰으로
     * when : 경로 즐겨찾기 추가 요청을 하면
     * then : 오류가 발생한다.
     */
    @DisplayName("올바르지 않은 인증 토큰으로 경로 즐겨찾기 추가 요청을 하면 오류가 발생한다.")
    @Test
    void addFavoriteWithWrongToken() {
        // given & when
        final var response = 즐겨찾기_추가_요청(WRONG_TOKEN, 강남역, 정자역);

        // then
        응답코드_400을_반환한다(response);
    }
}
