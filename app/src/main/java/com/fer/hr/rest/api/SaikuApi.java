package com.fer.hr.rest.api;

import com.fer.hr.rest.dto.discover.SimpleCubeElement;
import com.fer.hr.rest.dto.queryResult.QueryResult;
import com.fer.hr.rest.dto.license.License;
import com.fer.hr.rest.dto.discover.SaikuConnection;
import com.fer.hr.rest.dto.discover.SaikuCubeMetadata;
import com.fer.hr.rest.dto.session.Session;
import com.fer.hr.rest.dto.query2.ThinQuery;

import java.util.List;

import retrofit.Callback;
import retrofit.ResponseCallback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by igor on 18/12/15.
 */
public interface SaikuApi {

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

    @GET("/saiku/{username}/discover")
    void getConnectionsMetadata(@Path("username") String username, @Query("_") long currentMs, Callback<SaikuConnection[]> response);

    @GET("/saiku/{username}/discover")
    SaikuConnection[] getConnectionsMetadata(@Path("username") String username);

    @GET("/saiku/{username}/discover/{connection}/{catalog}/{schema}/{cube}/metadata")
    void getCubeMetadata(
            @Path("username") String username,
            @Path("connection") String connection,
            @Path("catalog") String catalog,
            @Path("schema") String schema,
            @Path("cube") String cube,
            Callback<SaikuCubeMetadata> response);

    @GET("/saiku/{username}/discover/{connection}/{catalog}/{schema}/{cube}/metadata")
    SaikuCubeMetadata getCubeMetadata(
            @Path("username") String username,
            @Path("connection") String connection,
            @Path("catalog") String catalog,
            @Path("schema") String schema,
            @Path("cube") String cube);

    @FormUrlEncoded
    @POST("/saiku/api/query/{queryname}")
    void createThinQuery(@Path("queryname")String queryname, @Field("json")String jsonThinQuery, Callback<ThinQuery> response);

    @POST("/saiku/api/query/execute")
    void executeThinQuery(@Body ThinQuery thinQuery,  Callback<QueryResult> response);

    @POST("/saiku/api/query/execute")
    Observable<QueryResult> executeThinQuery(@Body ThinQuery thinQuery);

    @FormUrlEncoded
    @POST("/authentication/register")
    void registerAccount(@Field("credentials")String credentials, @Field("gcmid")String gcmId, Callback<String> response);

    @FormUrlEncoded
    @POST("/authentication/register")
    String registerAccount(@Field("credentials")String credentials, @Field("gcmid")String gcmId);


    @FormUrlEncoded
    @POST("/authentication/login")
    void login(@Field("credentials")String credentials, Callback<String> response);

    @FormUrlEncoded
    @POST("/authentication/login")
    Observable<String> login(@Field("credentials")String credentials);

    @FormUrlEncoded
    @POST("/authentication/login")
    Observable<String> logout(@Field("credentials")String credentials);

    @FormUrlEncoded
    @POST("/authentication/logout")
    void logout(@Field("authenticationToken")String token, Callback<String> response);

//    @Path("/saiku/{username}/discover")
//    @Path("/{connection}/{catalog}/{schema}/{cube}/dimensions/{dimension}/hierarchies/{hierarchy}/levels/{level}")
    @GET("/saiku/{username}/discover/{connection}/{catalog}/{schema}/{cube}/dimensions/{dimension}/hierarchies/{hierarchy}/levels/{level}")
    void getLevelMembers(
            @Path("username")String username,
            @Path("connection")String connection,
            @Path("catalog")String catalog,
            @Path("schema")String schema,
            @Path("cube")String cube,
            @Path("dimension")String dimension,
            @Path("hierarchy")String hierarchy,
            @Path("level")String levelk,
            Callback<List<SimpleCubeElement>> callback);
}
