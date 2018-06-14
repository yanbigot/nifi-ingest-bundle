package com.yan.nifi.processors.training.httpclient

import com.yan.nifi.processors.training.config.TrainingConf._
import com.yan.nifi.processors.training.security.TrainingHelper._
import org.apache.http.client.methods.{ CloseableHttpResponse, HttpGet, HttpPost }
import org.apache.http.impl.client.HttpClientBuilder

import scala.io.Source
import scala.language.postfixOps
import scala.xml.XML

object TrainingHttpClient {
  val apiConf = getConf()
  val X_CSOD_API_KEY = "x-csod-api-key"
  val X_CSOD_SESSION_TOKEN = "x-csod-session-token"
  val X_CSOD_DATE = "x-csod-date"
  val X_CSOD_SIGNATURE = "x-csod-signature"
  val CONTENT_TYPE = "Content-Type"
  val ACCEPT = "Accept"
  val TEXT_XML = "text/xml"
  val MAP_TOKEN = "token"
  val MAP_ALIAS = "alias"
  val MAP_SECRET = "secret"

  def main(args: Array[String]): Unit = {
    val map = authenticate()
    println(map.toString())

    if (map.getOrElse("token", "").length > 0) {
      //view: String, token: String, alias: String, secret: String
      val x = callEndPoint("vw_rpt_training_base", map.getOrElse("token", ""), map.getOrElse("alias", ""), map.getOrElse("secret", ""))
    } else {
      println("AUTHENTICATION FAILED !!! ")
    }
  }

  def authenticate(): Map[String, String] = {
    val date = getDate
    val stringToSign = buildAuthStringToSign(date = date)
    val signature = generateSignature(apiConf.apiSecret, stringToSign, date)
    val url = this.buildAuthenticateUrl(date)
    val post: HttpPost = new HttpPost(url)
    post.addHeader(X_CSOD_DATE, date)
    post.addHeader(X_CSOD_SIGNATURE, signature)
    post.addHeader(X_CSOD_API_KEY, apiConf.apiId)
    post.addHeader(CONTENT_TYPE, TEXT_XML)
    post.addHeader(ACCEPT, TEXT_XML)
    post.addHeader("Connection", "keep_alive")
    post.expectContinue()

    val httpClient = HttpClientBuilder.create().build()
    val httpResponse = httpClient.execute(post)

    httpResponse.getAllHeaders.foreach(arg => println(arg))
    val entity = httpResponse.getEntity()
    var content = ""
    if (entity != null) {
      val inputStream = entity.getContent()
      content = Source.fromInputStream(inputStream).getLines.mkString("\n")
      inputStream.close
    }
    parseAutenticationResponse(content)
  }
  private def buildAuthenticateUrl(date: String): String = {
    apiConf.url + apiConf.authenticateEndpoint + "?userName=" + apiConf.userName + "&alias=" + date
  }

  def parseAutenticationResponse(content: String): Map[String, String] = {
    val xml = XML.loadString(content)
    val token = (xml \\ "Token") text
    val secret = (xml \\ "Secret") text
    val alias = (xml \\ "Alias") text

    println("content " + content)

    Map(
      MAP_TOKEN -> token,
      MAP_SECRET -> secret,
      MAP_ALIAS -> alias)
  }

  def call(view: String, token: String, alias: String, secret: String, filter: String): CloseableHttpResponse = {
    val date = getDate
    val stringToSign = buildEndPointCallStringToSign(date = date, token = token, httpUrl = view)
    val signature = generateSignature(secret, stringToSign, date)
    val url = buildCallEndPointUrl(view, filter)

    val get: HttpGet = new HttpGet(url)
    get.addHeader(X_CSOD_DATE, date)
    get.addHeader(X_CSOD_SIGNATURE, signature)
    get.addHeader(X_CSOD_SESSION_TOKEN, token)
    get.addHeader(CONTENT_TYPE, TEXT_XML)
    val httpClient = HttpClientBuilder.create().build()
    val httpResponse = httpClient.execute(get)
    httpResponse
  }

  def callEndPoint(view: String, token: String, alias: String, secret: String): String = {
    val date = getDate
    val stringToSign = buildEndPointCallStringToSign(date = date, token = token, httpUrl = view)
    val signature = generateSignature(secret, stringToSign, date)

    //todo val filter = "?$filter=" + filterCondition // xxx_dt gt cast('2016-08-16', Edm.DateTimeOffset)
    val url = buildCallEndPointUrl(view, "")
    val get: HttpGet = new HttpGet(url)
    get.addHeader(X_CSOD_DATE, date)
    get.addHeader(X_CSOD_SIGNATURE, signature)
    get.addHeader(X_CSOD_SESSION_TOKEN, token)
    get.addHeader(CONTENT_TYPE, TEXT_XML)

    //get.getAllHeaders.foreach(println)

    val httpClient = HttpClientBuilder.create().build()
    val httpResponse = httpClient.execute(get)
    //httpResponse.getAllHeaders.foreach(println)
    val entity = httpResponse.getEntity()
    var content = ""
    if (entity != null) {
      val inputStream = entity.getContent()
      content = Source.fromInputStream(inputStream).getLines.mkString("\n")
      inputStream.close
    }
    print("------------ get ---------------")
    println(content)
    content
  }
  //filter condition better be a case class to ease manipulation
  def buildCallEndPointUrl(view: String, filterCondition: String): String = {
    apiConf.url + apiConf.endpointOdataDomain + view + buildCallEndPointFilterCondition(filterCondition)
  }
  def buildCallEndPointFilterCondition(filterCondition: String): String = {
    filterCondition.isEmpty match {
      case false => "?$filter=" + filterCondition
      case true => ""
    }
  }

  def refreshToken(): Unit = {

  }
}
