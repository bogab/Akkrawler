package com.bogab.akkrawler

import java.io.{File, IOException}
import java.util.concurrent.TimeUnit

import okhttp3._

import scala.concurrent.{Future, Promise}

object Fetcher {

  val cacheSize: Long = 1024 * 1024 * 100
  val cache = new Cache(new File("data/cache"), cacheSize)

  private val client = new OkHttpClient()
    .newBuilder()
    .cache(cache)
    .followRedirects(true)
    .followSslRedirects(true)
    .readTimeout(30, TimeUnit.SECONDS)
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

  def fetch(url: String): Future[Response] = {
    val promise = Promise[Response]
    val future = promise.future
    val request = new Request.Builder()
      .url(url)
      .build()

    println(s"Fetching: $url")
    client.newCall(request).enqueue(new Callback {
      override def onFailure(call: Call, e: IOException): Unit = {
        promise.failure(e)
      }

      override def onResponse(call: Call, response: Response): Unit = {
        if (response.isSuccessful)
          promise.success(response)
        else
          promise.failure(new IOException("Unexpected code " + response))
      }
    })
    future
  }
}
