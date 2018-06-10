package com.yan.training.security

import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}
import com.yan.training.config.TrainingConf._
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter
import org.apache.commons.codec.binary.Base64

object TrainingHelper {
  val apiConf = getConf

  def buildAuthStringToSign(date: String): String = {
    "GET" + "\n" +
      "x-csod-api-key:" + apiConf.apiId + "\n" +
      "x-csod-date:" + date + "\n" +
      apiConf.authenticateEndpoint
  }

  def buildEndPointCallStringToSign( httpMethod: String = "POST", date: String, token: String, httpUrl: String): String = {
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
