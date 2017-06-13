package com.arts.data

import java.util.UUID

sealed abstract class Order {
  val uuid: UUID = java.util.UUID.randomUUID

  val client: String
  val security: Char
  val price: Int
  val quantity: Int

  override def toString = s"${this.getClass.getSimpleName}($client, $security, $price, $quantity)"

  def canEqual(other: Any): Boolean = other.isInstanceOf[Order]

  override def equals(other: Any): Boolean = other match {
    case that: Order =>
      (that canEqual this) &&
        uuid == that.uuid &&
        client == that.client &&
        security == that.security &&
        price == that.price &&
        quantity == that.quantity
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(uuid, client, security, price, quantity)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }
}

final case class BuyOrder(client: String, security: Char, price: Int, quantity: Int) extends Order
final case class SellOrder(client: String, security: Char, price: Int, quantity: Int) extends Order