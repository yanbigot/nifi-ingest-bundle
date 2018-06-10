package com.yan.nifi.processors.training.logic

import com.yan.nifi.processors.training.httpclient.TrainingHttpClient.{MAP_ALIAS, MAP_SECRET, MAP_TOKEN}
import com.yan.nifi.processors.training.model.Model.PageResult

import scala.language.postfixOps
import scala.xml.XML

class TrainingHttpClientManager {
  type Token = String

  def readConf(): Unit = {

  }
  def auth(): Either[Boolean, Token]  ={
    "a" match {
      case "a" => Right("")
      case _   => Left(false)
    }
  }
  def process(): Unit ={

  }

  def allFunctionalObjects(): Unit = {

  }
  def oneFunctionalObject(): Unit = {

  }
  //signature is mandatory for each endpoint call
  def oneEndpoint(): Unit = {
    //all infos

  }
  //write in stream mode and get
  def onePage(): Unit ={

  }
  //
  def writePage(pageResult: PageResult): Unit = {

  }
  //
  def pageAsMap(pageResult: PageResult): Unit = {

  }
  def accessTokenResponseToMap(content: String): Map[String, String] = {
    val xml = XML.loadString(content)
    val token = (xml \\ "Token") text
    val secret = (xml \\ "Secret") text
    val alias = (xml \\ "Alias") text

    Map(
      MAP_TOKEN -> token,
      MAP_SECRET -> secret,
      MAP_ALIAS -> alias)
  }
}
