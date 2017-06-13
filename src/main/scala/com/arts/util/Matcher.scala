package com.arts.util

import scala.collection.mutable

trait OrderedMultimap[A, B] extends mutable.MultiMap[A, B] {
  override def makeSet = new mutable.LinkedHashSet[B]
}

class Matcher[K, V](valueToKey: V => K) {
  private val map = new mutable.HashMap[K, mutable.Set[V]] with OrderedMultimap[K, V]

  def add(value: V): Unit = {
    map.addBinding(valueToKey(value), value)
  }

  def matchAndRemove(original: V, filter: V => Boolean): Option[V] = {
    val candidates = map.getOrElse(valueToKey(original), mutable.Set.empty)
    val matched = candidates.find(filter)
    if (matched.nonEmpty) {
      map.removeBinding(valueToKey(original), matched.get)
    }
    matched
  }

  def computeSize(): Int = map.values.map(_.size).sum
  def clear(): Unit = map.clear()

  override def toString = s"Matcher($map)"
}
