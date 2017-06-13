package com.arts.util

import org.scalatest.{BeforeAndAfter, FunSuite}

class MatcherSuite extends FunSuite with BeforeAndAfter {
  case class Car(maker: String, model: String, age: Int)
  val matcher = new Matcher[(String, String), Car](x => (x.maker, x.model))

  before {
    for (age <- 5 to 15 by 5) {
      matcher.add(Car("Honda", "Accord", age))
      matcher.add(Car("Ford", "Focus", age))
      matcher.add(Car("Chevrolet", "Corvette", age))
    }
  }

  after {
    matcher.clear()
  }

  test("check size") {
    assert(9 === matcher.computeSize)
  }

  test("should produce None if key doesn't exist") {
    assert(9 === matcher.computeSize)
    assert(matcher.matchAndRemove(Car("Ford", "Mustang", 3), _.age < 4).isEmpty)
  }

  test("should produce None if key exists but predicate is never satisfied") {
    assert(9 === matcher.computeSize)
    assert(matcher.matchAndRemove(Car("Ford", "Focus", 3), _.age < 4).isEmpty)
  }

  test("should pick first chronologically stored result if key exists and predicate is satisfied") {
    for (age <- 100 to 200) {
      matcher.add(Car("Ford", "Focus", age))
    }
    assert(110 === matcher.computeSize)
    var previousAge = 4
    var matched = matcher.matchAndRemove(Car("Ford", "Focus", 3), _.age > previousAge)
    while (matched.isDefined) {
      assert(previousAge < matched.get.age)
      previousAge = matched.get.age
      matched = matcher.matchAndRemove(Car("Ford", "Focus", 3), _.age > previousAge)
    }
    assert(6 === matcher.computeSize)
  }
}
