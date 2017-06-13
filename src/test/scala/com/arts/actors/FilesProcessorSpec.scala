package com.arts.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.arts.actors.FilesProcessor.{ResultSaved, StartProcessing}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class FilesProcessorSpec extends TestKit(ActorSystem("FilesProcessorSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Files processor" when {
    "sent a file to process" should {
      "produce a result equal to reference" in {
        val prefix = "src/test/resources/input/original"

        val resultPath = s"$prefix/result.txt"
        val filesProcessor = TestActorRef(new FilesProcessor(s"$prefix/clients.txt", s"$prefix/orders.txt", resultPath), s"files-processor")
        filesProcessor ! StartProcessing

        expectMsg(ResultSaved)

        val reference = scala.io.Source.fromFile(s"$prefix/reference.txt").mkString.replaceAll("\r", "")
        val result = scala.io.Source.fromFile(resultPath).mkString.replaceAll("\r", "")

        assert(result === reference)
      }
    }
  }
}
