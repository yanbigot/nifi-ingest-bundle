package com.socgen.nifi.processors.training

import java.text.SimpleDateFormat
import java.util.{ Calendar, TimeZone }

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import org.apache.commons.codec.binary.Base64

import scala.io.Source

object TrainingHelper {

  case class TrainingConf(apiId: String, apiSecret: String, userName: String)
  implicit val formats = net.liftweb.json.DefaultFormats
  def getConf(): TrainingConf = {
    val json = Source.fromURL(getClass.getResource("training/data.xml"))
    net.liftweb.json.parse(json.mkString).extract[TrainingConf]
  }

  def buildAuthStringToSign(httpMethod: String = "GET", apiId: String, date: String, httpUrl: String = "/services/api/sts/session"): String = {
    httpMethod + "\n" +
      "x-csod-api-key:" + apiId + "\n" +
      "x-csod-date:" + date + "\n" +
      httpUrl
  }

  def buildDataStringToSign(httpMethod: String = "POST", date: String, token: String, httpUrl: String): String = {
    httpMethod + "\n" +
      "x-csod-date:" + date + "\n" +
      "x-csod-session-token:" + token + "\n" +
      httpUrl
  }

  def generateSignature(key: String, message: String, date: String, algo: String = "HmacSHA512"): String = {
    var encoded = ""
    try {
      val keyBytesDecoded = DatatypeConverter.parseBase64Binary(key)
      val messageBytes = message.getBytes("UTF-8")
      val HMAC = Mac.getInstance(algo)
      val secretKey = new SecretKeySpec(keyBytesDecoded, algo)
      HMAC.init(secretKey)
      val hash = HMAC.doFinal(messageBytes)
      encoded = Base64.encodeBase64String(hash)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    encoded
  }

  def getDate(): String = {

    val today = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime()
    val yearMonthDay = new SimpleDateFormat("YYYY-MM-dd")
    val hour = new SimpleDateFormat("HH")
    val minutes = new SimpleDateFormat("mm")
    val secondes = new SimpleDateFormat("ss")
    val currentHourMinus1 = hour.format(today).toInt - 1
    val currentSecondesMinus4 = secondes.format(today).toInt - 4
    val currentYearMonthDay = yearMonthDay.format(today)
    val currentMinutes = minutes.format(today)

    new String(currentYearMonthDay + "T" + currentHourMinus1 + ":" + currentMinutes + ":" + currentSecondesMinus4 + ".000")
  }
}
