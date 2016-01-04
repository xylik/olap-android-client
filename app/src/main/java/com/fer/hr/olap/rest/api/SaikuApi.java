package com.fer.hr.olap.rest.api;

import com.fer.hr.olap.rest.dto.queryResult.QueryResult;
import com.fer.hr.olap.rest.dto.license.License;
import com.fer.hr.olap.rest.dto.discover.SaikuConnection;
import com.fer.hr.olap.rest.dto.discover.SaikuCubeMetadata;
import com.fer.hr.olap.rest.dto.session.Session;
import com.fer.hr.olap.rest.dto.query2.ThinQuery;
import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by igor on 18/12/15.
 */
public interface SaikuApi {


    //Field -> corresponds to form field, more form fields can be gruped into (@FieldMap Map<String, String> form)
    @FormUrlEncoded
    @POST("/session")
    void createSession(
            @Field("username") String username,
            @Field("password") String password,
            @Field("language") String language,
            ResponseCallback response
    );

    @GET("/session")
    void getSession(@Query("_") long currentMs, Callback<Session> response);

    @GET("/api/license")
    void getLicense(@Query("_") long currentMs, Callback<License> response);

    @GET("/{username}/discover")
    void getConnectionsMetadata(@Path("username") String username, @Query("_") long currentMs, Callback<SaikuConnection[]> response);

    @GET("/{username}/discover/{connection}/{catalog}/{schema}/{cube}/metadata")
    void getCubeMetadata(
            @Path("username") String username,
            @Path("connection") String connection,
            @Path("catalog") String catalog,
            @Path("schema") String schema,
            @Path("cube") String cube,
            @Query("key") String key,
            @Query("_") long currentMs,
            Callback<SaikuCubeMetadata> response);

    @FormUrlEncoded
    @POST("/api/query/{queryname}")
    void createThinQuery(@Path("queryname")String queryname, @Field("json")String jsonThinQuery, Callback<ThinQuery> response);

    @POST("/api/query/execute")
    void executeThinQuery(@Body ThinQuery thinQuery,  Callback<QueryResult> response);

    @POST("/api/query/execute")
    void executeThinQuery2(@Body String thinQueryJson,  Callback<QueryResult> response);
}
