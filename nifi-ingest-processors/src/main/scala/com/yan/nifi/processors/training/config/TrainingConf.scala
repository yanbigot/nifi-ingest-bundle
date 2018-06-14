package com.yan.nifi.processors.training.config

import com.yan.nifi.processors.training.model.Model.TrainingCredentials

import scala.io.Source

object TrainingConf {

  implicit val formats = net.liftweb.json.DefaultFormats
  def getConf(): TrainingCredentials = {
    //    val json = Source.fromURL(getClass.getResource("training\\training.json"))
    val json = Source.fromFile("C:\\Users\\MonMien\\nifi-ingest-bundle\\nifi-ingest-processors\\src\\main\\resources\\training\\training.json")
    net.liftweb.json.parse(json.mkString).extract[TrainingCredentials]
  }
}
