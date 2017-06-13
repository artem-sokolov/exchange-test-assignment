package com.arts.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.arts.actors.Book.{OrderAccepted, OrderMatch}
import com.arts.actors.Exchange.ReturnResultWhenItsReady
import com.arts.data.{Clients, Order}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object Exchange {
  def props(clients: Clients): Props = Props(new Exchange(clients))

  final case object ReturnResultWhenItsReady
}

class Exchange(clients: Clients) extends Actor with ActorLogging {
  var totalOrdersCount = 0

  var unacceptedOrders = mutable.Set.empty[Order] // to track status of orders submitted to books
  var unmatchedOrders = mutable.Set.empty[Order] // to track status of orders submitted to books

  var securityToBook = mutable.Map.empty[Char, ActorRef]

  override def preStart(): Unit = {
    log.info(s"exchange initialized with: $clients")
    log.info("ready to accept orders...")
  }

  override def postStop(): Unit = log.info("exchange stopped")

  val commonReceive: Receive = {
    case orderMatch@OrderMatch(order1, order2) =>
      if (unmatchedOrders.contains(order1) && unmatchedOrders.contains(order2)) {
        clients.processOrderMatch(orderMatch)
        unacceptedOrders -= order1
        unacceptedOrders -= order2
        unmatchedOrders -= order1
        unmatchedOrders -= order2
        log.debug(s"executed orders $order1 and $order2")
      }
    case OrderAccepted(order) =>
      unacceptedOrders -= order
      log.debug(s"order $order was accepted to corresponding book")
  }

  def retryingUnacceptedOrders(originalRequester: ActorRef): Receive = {
    case message@ReturnResultWhenItsReady =>
      if (unacceptedOrders.isEmpty) {
        originalRequester ! clients
      } else {
        log.debug(s"retrying = $unacceptedOrders")
        unacceptedOrders.foreach(order => sendOrderToBook(order))
        context.system.scheduler.scheduleOnce(2 seconds, self, message)
      }
  }

  val acceptingNewOrders: Receive = {
    case order: Order =>
      totalOrdersCount += 1
      sendOrderToBook(order)
      unacceptedOrders += order
      unmatchedOrders += order
    case ReturnResultWhenItsReady =>
      log.info(s"received end of orders after $totalOrdersCount orders")
      context become (commonReceive orElse retryingUnacceptedOrders(sender))
      self ! ReturnResultWhenItsReady
  }

  override def receive: Receive = commonReceive orElse acceptingNewOrders

  private def sendOrderToBook(order: Order) = {
    val security = order.security
    securityToBook.get(security) match {
      case Some(book) =>
        book ! order
      case None =>
        val bookActor = context.actorOf(Book.props(security), s"book-$security")
        securityToBook += security -> bookActor
        bookActor ! order
    }
  }
}
