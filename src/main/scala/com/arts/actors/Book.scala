package com.arts.actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.arts.actors.Book.{OrderAccepted, OrderMatch}
import com.arts.data.{BuyOrder, Order, SellOrder}
import com.arts.util.Matcher

import scala.collection.mutable

object Book {
  def props(security: Char): Props = Props(new Book(security))

  final case class OrderMatch(order1: Order, order2: Order)
  final case class OrderAccepted(order: Order)
}

class Book(security: Char) extends Actor with ActorLogging {
  type OrderMatcher = Matcher[(Int, Int), Order]

  var unexecutedOrders = mutable.Set.empty[UUID] // to avoid accepting duplicate orders during retrying phase

  private val buyOrders, sellOrders = new OrderMatcher(order => (order.price, order.quantity))

  override def preStart(): Unit = log.info(s"book for security '$security' started")
  override def postStop(): Unit = log.info(s"book for security '$security' stopped")

  override def receive: Receive = {
    case order: BuyOrder =>
      processOrder(sender, sellOrders, buyOrders, order)
    case order: SellOrder =>
      processOrder(sender, buyOrders, sellOrders, order)
  }

  def processOrder(sender: ActorRef, ordersForMatching: OrderMatcher, ordersForAdding: OrderMatcher, order: Order): Unit = {
    val matchedOrder = ordersForMatching.matchAndRemove(order, _.client != order.client)
    matchedOrder match {
      case Some(matched) =>
        unexecutedOrders -= order.uuid
        unexecutedOrders -= matched.uuid
        sender ! OrderMatch(order, matched)
      case None =>
        if (!unexecutedOrders.contains(order.uuid)) {
          ordersForAdding.add(order)
          unexecutedOrders += order.uuid
          sender ! OrderAccepted(order)
        }
    }
  }

  def computeUnexecutedSize(): Int = buyOrders.computeSize + sellOrders.computeSize
}