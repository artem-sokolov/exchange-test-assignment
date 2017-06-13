package com.arts.data

import com.arts.actors.Book.OrderMatch

import scala.collection.mutable

case class Clients(balances: mutable.LinkedHashMap[String, mutable.LinkedHashMap[Char, Int]], currency: Char = '$') {
  def processOrderMatch(orderMatch: OrderMatch): Unit = {
    processOrder(orderMatch.order1)
    processOrder(orderMatch.order2)
  }

  private def processOrder(order: Order): Unit = {
    val balance = balances(order.client)
    order match {
      case BuyOrder(_, security, price, quantity) =>
        balance(security) += quantity
        balance(currency) -= quantity * price
      case SellOrder(_, security, price, quantity) =>
        balance(security) -= quantity
        balance(currency) += quantity * price
    }
  }
}
