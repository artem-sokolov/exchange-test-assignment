package com.arts.actors

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.arts.actors.Book.{OrderAccepted, OrderMatch}
import com.arts.data.{BuyOrder, SellOrder}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration.DurationInt

class BookSpec extends TestKit(ActorSystem("BookSpec")) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Book actor" when {
    "submitted two orders from two different clients with same quantity and price" should {
      "match them" in {
        val aBook = TestActorRef(new Book('A'))
        val buyOrder = BuyOrder("C1", 'A', 10, 20)
        val sellOrder = SellOrder("C2", 'A', 10, 20)
        aBook ! buyOrder
        expectMsg(OrderAccepted(buyOrder))
        aBook ! sellOrder
        expectMsg(OrderMatch(sellOrder, buyOrder))
      }
    }
    "submitted two orders from same client even with same quantity and price" should {
      "not match them" in {
        val aBook = TestActorRef(new Book('A'))
        val buyOrder = BuyOrder("C1", 'A', 10, 20)
        val sellOrder = SellOrder("C1", 'A', 10, 20)
        aBook ! buyOrder
        expectMsg(OrderAccepted(buyOrder))
        aBook ! sellOrder
        expectMsg(OrderAccepted(sellOrder))
        expectNoMsg(300 milliseconds)
      }
    }
    "submitted two orders from same different clients with different quantity and price" should {
      "not match them" in {
        val aBook = TestActorRef(new Book('A'))
        val buyOrder = BuyOrder("C1", 'A', 10, 20)
        val sellOrder = SellOrder("C2", 'A', 30, 40)
        aBook ! buyOrder
        expectMsg(OrderAccepted(buyOrder))
        aBook ! sellOrder
        expectMsg(OrderAccepted(sellOrder))
        expectNoMsg(300 milliseconds)
      }
    }
    "submitted duplicate orders" should {
      "accept only one" in {
        val aBook = TestActorRef(new Book('A'))
        val buyOrder = BuyOrder("C1", 'A', 10, 20)
        aBook ! buyOrder
        aBook ! buyOrder
        aBook ! buyOrder
        aBook ! buyOrder
        expectMsg(OrderAccepted(buyOrder))
        assert(1 === aBook.underlyingActor.computeUnexecutedSize())
      }
    }
  }

  "Book actor" should {
    "delete matched orders from unexecuted" in {
      val aBook = TestActorRef(new Book('A'))
      val buyOrder = BuyOrder("C1", 'A', 10, 20)
      val sellOrder = SellOrder("C2", 'A', 10, 20)
      aBook ! buyOrder
      expectMsg(OrderAccepted(buyOrder))
      aBook ! sellOrder
      expectMsg(OrderMatch(sellOrder, buyOrder))
      assert(0 === aBook.underlyingActor.computeUnexecutedSize())
      aBook ! buyOrder
      expectMsg(OrderAccepted(buyOrder))
      assert(1 === aBook.underlyingActor.computeUnexecutedSize())
    }
  }
}