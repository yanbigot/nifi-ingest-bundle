package com.yan.nifi.processors.training.model

object Model {

  case class AccessTokenResponse(token: String, secret: String, alias: String)

  case class PageResult(`@odata.context`: String, value: List[Map[String, String]], isLastPage: String, nextLink: Option[String])

  case class TrainingCredentials(apiId: String, apiSecret: String, userName: String, url: String, authenticateEndpoint: String, endpointOdataDomain: String)

  case class CallParams(view: String, token: AccessTokenResponse, filter: String)

  //Definition of the business object to build through endpoint calls
  case class FuncObj(name: String,
    endpoint: String,
    dateFilter: DateFilter,
    joinConditions: List[JoinCondition],
    children: List[FuncObj])
  case class DateFilter(dateFrom: String, dateFromField: String, dateTo: String, dateToField: String)
  case class JoinConditions(joinConditions: List[JoinCondition])
  case class JoinCondition(parent: JoinField, child: JoinField, operator: String)
  case class JoinField(name: String, dataType: String)

  val mockDefinition = List(
    FuncObj(
      "root",
      "vw_rpt_training_base",
      dateFilter,
      null,
      children))
  val dateFilter = DateFilter("2000", "last_mod_dt", "2018", "last_mod_dt")
  val children = List(
    FuncObj(
      "c1",
      "vw_rpt_training_cf",
      dateFilter,
      joinConditions,
      null))
  val joinConditions = List(
    JoinCondition(
      JoinField("a", "string"),
      JoinField("b", "string"),
      "eq"))

  //  object DateFilter{
  //    def apply(dateFrom: String, dateFromField: String, dateTo: String, dateToField: String): String = {
  //      dateFromField + " gt cast('" + dateFrom + "', Edm.DateTimeOffset)" +
  //      " and " +
  //      dateToField + " lt cast('" + dateTo + "', Edm.DateTimeOffset)"
  //    }
  //  }
}
