package com.yan.nifi.processors.training.model

object Model {

  case class Result( headers: HeaderResult, page: PageResult )

  case class HeaderResult( )

  case class PageResult( `@odata.context`: String, value: List[Map[String, String]], isLastPage: String, nextLink: Option[String] )

  case class TrainingCredentials( apiId: String, apiSecret: String, userName: String, url: String, authenticateEndpoint: String )

}
