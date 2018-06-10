package com.yan

import com.yan.nifi.processors.training.httpclient.TrainingHttpClient
import org.junit.Test

class TrainingHttpClientJunitTest {
  @Test
  def auth(): Unit ={
    val tokenRes = TrainingHttpClient.authenticate()
    assert(tokenRes.contains("token"))
  }

}
