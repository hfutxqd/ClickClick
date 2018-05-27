package xyz.imxqd.clickclick.model.web;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Url;
import xyz.imxqd.clickclick.R;

public interface ServerApi {
    String BASE_URL = "https://click.nodate.date";

    @GET("list")
    Observable<HttpResult<HomePage>> listInfo();

    @GET
    Observable<HttpResult<HomePage>> loadList(@Url String url);

}
