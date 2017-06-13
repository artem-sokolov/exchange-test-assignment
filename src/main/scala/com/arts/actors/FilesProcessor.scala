package com.arts.actors

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.arts.actors.Exchange.ReturnResultWhenItsReady
import com.arts.actors.FilesProcessor.{ResultSaved, StartProcessing}
import com.arts.data.{BuyOrder, Clients, SellOrder}

import scala.collection.mutable
import scala.io.Source

object ClientsFromFile {
  def load(clientsPath: String): Clients = {
    val balancesRaw = Source.fromFile(clientsPath).getLines().map(_.stripLineEnd.trim.split("\t")) toList
    val balances = new mutable.LinkedHashMap[String, mutable.LinkedHashMap[Char, Int]]

    balancesRaw.foreach { splitLine =>
      val client = splitLine.head
      val amounts = new mutable.LinkedHashMap[Char, Int]
      ('$' :: ('A' to 'Z' toList) zip splitLine.tail.map(_.toInt)).foreach { case (security, amount) =>
        amounts += (security -> amount)
      }
      balances += (client -> amounts)
    }

    Clients(balances)
  }
}

object FilesProcessor {
  def props(clientsPath: String, ordersPath: String, resultPath: String): Props = Props(new FilesProcessor(clientsPath, ordersPath, resultPath))

  final case object StartProcessing
  final case object ResultSaved
}

class FilesProcessor(clientsPath: String, ordersPath: String, resultPath: String) extends Actor with ActorLogging {
  private val exchange = context.actorOf(Exchange.props(ClientsFromFile.load(clientsPath)), "exchange")

  def waitingForResults(originalRequester: ActorRef): Receive = {
    case clients: Clients =>
      val str = clients.balances.map { case (k, v) =>  (k :: v.values.map(_.toString).toList).mkString("\t")}.mkString("\n")
      Files.write(Paths.get(resultPath), str.getBytes(StandardCharsets.UTF_8))
      originalRequester ! ResultSaved
  }

  override def receive: Receive = {
    case StartProcessing =>
      Source.fromFile(ordersPath).getLines().foreach { line =>
        val lineSplit = line.trim().split("\t")
        lineSplit match {
          case Array(client, "s", security, price, quantity) =>
            val order = SellOrder(client, security.head, price.toInt, quantity.toInt)
            exchange ! order
          case Array(client, "b", security, price, quantity) =>
            val order = BuyOrder(client, security.head, price.toInt, quantity.toInt)
            exchange ! order
          case _ =>
            log.warning(s"couldn't parse an order from line '$line'")
        }
      }

      context become waitingForResults(sender)

      exchange ! ReturnResultWhenItsReady
  }
}
