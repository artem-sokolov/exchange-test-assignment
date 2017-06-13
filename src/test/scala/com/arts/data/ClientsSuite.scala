package com.arts.data

import com.arts.actors.Book.OrderMatch
import org.scalatest.FunSuite

import scala.collection.mutable

class ClientsSuite extends FunSuite {
  type LHMap[K, V] = mutable.LinkedHashMap[K, V]
  type BalanceMap = LHMap[Char, Int]

  implicit def mapToLHMap[K, V](x: Map[K, V]): LHMap[K, V] = mutable.LinkedHashMap(x.toSeq : _*)

  test("simple test") {
    val balance1: BalanceMap  = Map('$' -> 100, 'A' -> 10, 'B' -> 20)
    val balance2: BalanceMap = Map('$' -> 300, 'A' -> 0, 'B' -> 0)
    val clients = Clients(Map("C1" -> balance1, "C2" -> balance2))

    val order1 = BuyOrder("C2", 'B', 15, 10)
    val order2 = SellOrder("C1", 'B', 15, 10)

    clients.processOrderMatch(OrderMatch(order1, order2))

    val referenceBalance1: BalanceMap = Map('$' -> 250, 'A' -> 10, 'B' -> 10)
    val referenceBalance2: BalanceMap = Map('$' -> 150, 'A' -> 0, 'B' -> 10)
    val referenceClients = Clients(Map("C1" -> referenceBalance1, "C2" -> referenceBalance2))

    assert(clients == referenceClients)
  }
}
