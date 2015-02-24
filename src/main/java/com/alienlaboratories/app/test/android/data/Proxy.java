// Copyright (c) 2013 Alien Laboratories, Inc.

package com.alienlaboratories.app.test.android.data;

import android.os.AsyncTask;
import android.util.Log;
import com.alienlaboratories.db.ItemProto;
import com.alienlaboratories.db.action.ActionProto;
import com.alienlaboratories.db.mutation.MutationProto;
import com.alienlaboratories.db.query.QueryProto;
import com.alienlaboratories.net.NetProto.Request;
import com.alienlaboratories.net.NetProto.Response;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import org.apache.http.HttpStatus;

import javax.annotation.Nullable;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Utils.
 */
//@Singleton
// TODO(burdon): Refactor as async.Conduit.
public final class Proxy {

  private static final String LOG = Proxy.class.getSimpleName();

  // TODO(burdon): Proto Lite!!!

  // TODO(burdon): Tests for JSON/serialization.
  public static <T extends Message> void test(Class<T> clazz, T obj) {
    try {
      byte[] serialized = obj.toByteArray();
      Request.parseFrom(serialized);
      Log.d(LOG, "Encode: " + serialized.length);
    } catch (RuntimeException ex) {
      ex.printStackTrace();
    } catch (InvalidProtocolBufferException ex) {
      throw new RuntimeException(ex);
    }
  }

  // TODO(burdon): Test serialization (and prevent proguard stripping).
  public static void tests() {
    Proxy.test(Request.class, Request.newBuilder()
        .setHeader(Request.Header.newBuilder().setService("xyz"))
        .addExtension(QueryProto.query, QueryProto.Query.newBuilder().build())
        .addExtension(MutationProto.mutation, MutationProto.Mutation.newBuilder()
            .addItem(ItemProto.Item.newBuilder()
                .setType("test")
                .setData(ItemProto.Item.Data.newBuilder()
                    .setExtension(ItemProto.summary, ItemProto.Summary.newBuilder()
                        .setTitle("")
                        .setDescription("")
                        .build()))).build())
        .addExtension(ActionProto.action, ActionProto.Action.newBuilder().setCommand("test").build())
        .build());

    Proxy.test(Response.class, Response.newBuilder()
        .setHeader(Response.Header.newBuilder()
            .setStatus(Response.Status.newBuilder().setText("OK")))
        .addExtension(QueryProto.result, QueryProto.Result.newBuilder().setQueryVersion(0).build())
        .build());
  }

  // TODO(burdon): Google Cloud Endpoints? https://developers.google.com/appengine/docs/java/endpoints/
  // https://code.google.com/p/cloud-tasks-io/source/browse/#svn%2Ftrunk%2FCloudTasks-Android%2Fsrc%2Fcom%2Fcloudtasks
  // http://www.tbray.org/ongoing/When/201x/2011/09/29/AERC

  public static final String ACCOUNT_TYPE = "com.google";

  public static final String AUTH_TOKEN_TYPE = "ah";

  // TODO(burdon): Factor out (inject).
  public static final String SITE_URL = "nexus-demo.appspot.com";

  public static final String LOGIN_URL = "http://" + SITE_URL + "/_ah/login?continue=http://localhost&auth=%s";

  public static final String DATA_URL = "http://" + SITE_URL + "/nx/data";

  // TODO(burdon): Singleton instance.
  private static final Proxy proxy = new Proxy();
  public static Proxy getInstance() {
    return proxy;
  }

  // http://developer.android.com/reference/java/net/HttpURLConnection.html
  // TODO(burdon): Use this (persistent cookie store: http://loopj.com/android-async-http)
  private final CookieManager cookieManager = new CookieManager();

  private Proxy() {
    CookieHandler.setDefault(cookieManager);

    // http://stackoverflow.com/questions/12319194/android-httpurlconnection-throwing-eofexception
    // http://stackoverflow.com/questions/8487780/weird-eofexception-on-galaxy-nexus
    System.setProperty("http.keepAlive", "false");
  }

  /**
   * Returns the authentication cookie from the default cookie manager.
   */
  // http://stackoverflow.com/questions/9811730/login-to-appengine-from-android-client
  public String getAuthCookie() {
    for (HttpCookie cookie : cookieManager.getCookieStore().getCookies()) {
      if (cookie.getName().equals("ACSID") || cookie.getName().equals("SACSID")) {
        return cookie.getValue();
      }
    }

    return null;
  }

  public void doAsyncPost(Request request) {
    doAsyncPost(request, null);
  }
  public void doAsyncPost(Request request, @Nullable Callback callback) {
    new PostTask(callback).execute(request);
  }

  public interface Callback {

    void onResponse(Response response);
  }

  /**
   * Async posting task.
   */
  private class PostTask extends AsyncTask<Request, Void, Response> {

    @Nullable
    private final Callback callback;

    PostTask(@Nullable  Callback callback) {
      this.callback = callback;
    }

    @Override
    protected Response doInBackground(Request... request) {
      Response.Builder response = Response.newBuilder();

      HttpURLConnection connection = null;
      try {
        // Encode request.
        Log.d(LOG, "Request: " + request[0]);

        // Post request.
        // TODO(burdon): Authentication? Skip by setting up non-auth servlet (or temp disable).
        URL url = new URL(DATA_URL);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setChunkedStreamingMode(0);
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty("Cookie", getAuthCookie()); // TODO(burdon): Should be set automatically via CookieHandler!!!
//      connection.addRequestProperty("X-Same-Domain", "1"); // XSRF

        Log.v(LOG, "Request: " + url);
        /*
        for (String p : connection.getRequestProperties().keySet()) {
          Log.v(LOG, "PROP[" + p + "]=" + connection.getRequestProperty(p));
        }
        */

        // Write JSON body.
        OutputStream out = connection.getOutputStream();
        request[0].writeTo(out);
        out.flush();

        // Parse response.
        int status = connection.getResponseCode();
        if (status != HttpStatus.SC_OK) {
          Log.w(LOG, "Status code: " + status);
        } else {
          response.mergeFrom(connection.getInputStream());
          Log.d(LOG, "Received: " + response);
        }
      } catch (Exception ex) {
        Log.e(LOG, "Error sending: " + ex);
        ex.printStackTrace();
      } finally {
        if (connection != null) {
          connection.disconnect();
        }
      }

      return response.build();
    }

    @Override
    protected void onPostExecute(Response response) {
      Log.d(LOG, "Response: " + response);
      if (callback != null) {
        callback.onResponse(response);
      }
    }
  }
}
