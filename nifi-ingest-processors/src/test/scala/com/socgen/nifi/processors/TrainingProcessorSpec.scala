/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.socgen.nifi.processors

import java.io._

// ScalaTest
import org.scalatest._

// NiFi
import org.apache.nifi.util.{ TestRunner, TestRunners }

class TrainingProcessorSpec extends FunSpec {
  import scala.collection.JavaConverters._
  import TrainingProcessorProperties.ExampleProperty
  import TrainingProcessorRelationships.{ RelSuccess, RelFailure }

  val SomeContent = "some content"

  describe("TrainingProcessor") {
    it("should successfully transfer a FlowFile") {
      val processor = new TrainingProcessor
      val runner = TestRunners.newTestRunner(processor)
      runner.setProperty(ExampleProperty, "1234")

      val content = new ByteArrayInputStream(SomeContent.getBytes)
      runner.enqueue(content)
      runner.run(1)

      runner.assertTransferCount(RelSuccess, 1)
      runner.assertTransferCount(RelFailure, 0)

      for (flowFile <- runner.getFlowFilesForRelationship(RelSuccess).asScala) {
        flowFile.assertContentEquals(SomeContent)
      }
    }
  }
}
