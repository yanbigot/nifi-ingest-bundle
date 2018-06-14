package com.yan.nifi.processors.training.logic

import com.yan.nifi.processors.training.httpclient.TrainingClient
import com.yan.nifi.processors.training.httpclient.TrainingHttpClient.{ MAP_ALIAS, MAP_SECRET, MAP_TOKEN }
import com.yan.nifi.processors.training.model.Model
import com.yan.nifi.processors.training.model.Model._
import net.liftweb.json._

import scala.language.postfixOps
import scala.xml.XML

class ProcessorLogic {
  type Token = String
  val httpClient = new TrainingClient

  def readConf(): Unit = {

  }

  def auth(retry: Int): Either[Boolean, AccessTokenResponse] = {
    (httpClient.authenticate, retry > 0) match {
      case (Right(token), false) => Right(token)
      case (Left(err), false) => auth(retry - 1)
      case (Left(err), true) => throw new Exception("Unable to authenticate, max retry reached, last error message : {" + err + "}")
    }
  }

  def process(): Unit = {
    val token = auth(5).right.get
    val conf = Model.mockDefinition
    val root = conf
      .map(root => CallParams(root.endpoint, token, this.filterAsStringFromDateFilter(root.dateFilter)))
      .map(p => httpClient.call(p.view, p.token.token, p.token.secret, p.token.alias, p.filter))
  }

  //signature is mandatory for each endpoint call
  def processEndpoint(functionalObject: FuncObj, p: CallParams): (PageResult, AccessTokenResponse) = {
    //all infos
    val jsonBody = httpClient.call(p.view, p.token.token, p.token.secret, p.token.alias, p.filter)
    val nl = nextLink(jsonBody)
    (null, null)
  }

  //write in stream mode and get
  def processPage(): Unit = {

  }

  //
  def writePage(pageResult: PageResult): Unit = {

  }

  //
  def pageAsMap(pageResult: PageResult): Unit = {

  }

  def nextLink(content: String): Either[Boolean, String] = {
    val json = parse(content)

    Option(json \\ "nextLink") match {
      case Some(nextLink) => Right(nextLink.asInstanceOf[String])
      case None => Left(false)
    }
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

  def filterAsStringFromDateFilter(df: DateFilter): String = {
    df.dateFromField + " gt cast('" + df.dateFrom + "', Edm.DateTimeOffset)" +
      " and " +
      df.dateToField + " lt cast('" + df.dateTo + "', Edm.DateTimeOffset)"
  }

}

object ProcessorLogic {
  val j = """{"nextLink": "12}"""

  def main(args: Array[String]): Unit = {
    val pl = new ProcessorLogic
    val r = pl.nextLink(j)
  }
}
