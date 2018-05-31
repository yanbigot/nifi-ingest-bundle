package com.socgen.nifi.processors.training

import scala.io.Source
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.DefaultHttpClient

object TrainingHttpClient {

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

  val endPointConfs: Seq[EndPointConf] = Seq()
  case class EndPointConf(
                        name: String,
                        uri: String,
                        filter: String,
                        idFieldName: String,
                        idFieldType: String,
                        children: EndPointConf)
  case class FunctionalObject(
                        name: String,
                        document: String,
                        children: Seq[FunctionalObject])

  /**
    * Processing flow
    * - auth
    * - config with
    * @param args
    */
  def main(args: Array[String]) {
    val map = authenticate()
    println(map.toString())

    for(endPointCOnf <- endPointConfs){
      processEndPoint(endPointCOnf)
    }

    if(map.getOrElse("token", "").length > 0 ){
      //view: String, token: String, alias: String, secret: String
      callEndPoint("vw_rpt_training_base", map.getOrElse("token", ""), map.getOrElse("alias",""), map.getOrElse("secret",""))
    }
    else{
      println("AUTHENTICATION FAILED !!! ")
    }
  }

  def authenticate(): Map[String, String] ={
    val url = "https://socgen-stg.csod.com"
    val endpoint = "/services/api/sts/session"
    val userName = ApiCode.getProperty(ApiCode.USER_NAME)
    val httpMethod = "POST"
    val userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17')"
    val apiId = ApiCode.getProperty(ApiCode.API_ID)
    val apiSecret = ApiCode.getProperty(ApiCode.API_SECRET)
    val keyName = "x-csod-api-key"
    val date = TrainingHelper.getDate
    val stringToSign = TrainingHelper.buildStringToSign(date,httpMethod, keyName, apiId, sessionUrl)
    val signature = HMACgen.generateSignature(apiSecret, stringToSign, date, "", "HmacSHA512")
    val post: HttpPost = new HttpPost(url + endpoint + "?userName=" + userName + "&alias=" + date)
    post.addHeader(X_CSOD_DATE , date)
    post.addHeader(X_CSOD_SIGNATURE , signature)
    post.addHeader(X_CSOD_API_KEY , apiId )
    post.addHeader(CONTENT_TYPE , TEXT_XML)
    post.addHeader(ACCEPT , TEXT_XML)
    post.addHeader("Connection", "keep_alive")
    post.expectContinue()

    val httpClient = new DefaultHttpClient()
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

  def parseAutenticationResponse(content: String): Map[String, String] ={
    val xml = XML.loadString(content)
    val token  = (xml \\ "Token" ) text
    val secret = (xml \\ "Secret") text
    val alias = (xml \\ "Alias") text

    println("content "+content)

    Map(
      MAP_TOKEN -> token,
      MAP_SECRET -> secret,
      MAP_ALIAS -> alias
    )
  }

  def processEndPoint(endPointCOnf: EndPointConf): Unit ={
    //val cur = call(params)
    //val fObj = toObj(cur)
    //val ids = extractFieldIds
    //for(id <- ids){
    // val children = call(params, id)
    // fObj
    // for
    // }

  }

  /**
    *
    *
    $httpMethod = 'GET';
    $key = 'x-csod-session-token:'.$token;
    $httpUrl = $url;
    date_default_timezone_set('UTC');
    $date = 'x-csod-date:'.date('Y-m-d').'T'.date('H:i:s').'.000';
    $stringToSign = $httpMethod."\n".$date."\n".$key."\n".$httpUrl;
    //Generate the signature

    $secretKey = base64_decode($secret);

    $signature = base64_encode(hash_hmac('sha512', $stringToSign, $secretKey,true));
    */
  def callEndPoint(view: String, token: String, alias: String, secret: String): String = {

    val keyName = "x-csod-session-token"
    val date = TrainingHelper.getDate
    //to use string to sign
    val stringToSign = "GET\nx-csod-date:"+date+"\nx-csod-session-token:"+token+"\n"+view

    val signature = HMACgen.generateSignature(
      secret,
      stringToSign,
      date,
      "",
      "HmacSHA512")

    //todo val filter = "?$filter=" + filterCondition // xxx_dt gt cast('2016-08-16', Edm.DateTimeOffset)
    val url = "https://socgen-stg.csod.com"+view //+ "?$filter=lo_modified_dt=2013-4-3"
    val get: HttpGet = new HttpGet(url )
    get.addHeader(X_CSOD_DATE , date)
    get.addHeader(X_CSOD_SIGNATURE , signature)
    get.addHeader(X_CSOD_SESSION_TOKEN , token )
    get.addHeader(CONTENT_TYPE, TEXT_XML)

    get.getAllHeaders.foreach(println)

    val httpClient = new DefaultHttpClient()
    val httpResponse = httpClient.execute(get)
    httpResponse.getAllHeaders.foreach(println)
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

}
