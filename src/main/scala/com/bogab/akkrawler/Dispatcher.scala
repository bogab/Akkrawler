package com.bogab.akkrawler

import akka.actor.{Actor, ActorRef, Props}

case class StartCrawl()

case class ParseComplete(url: String, links: Set[String])

class Dispatcher(seed: String, maxPages: Int) extends Actor {

  private var LIMIT = 1 //the seed
  var visited = Set.empty[String]
  var ongoing = Map.empty[String, ActorRef]

  override def receive = {
    case StartCrawl =>
      val a = context.actorOf(Props[Parser](new Parser(seed)))
      ongoing += (seed -> a)
    case ParseComplete(url, links) =>
      visited += url
      ongoing -= url
      context.stop(sender())
      crawlLinks(links)
  }

  def crawlLinks(links: Set[String]): Unit = {
    for (link <- links) {
      if (!visited.contains(link) && !ongoing.contains(link) && LIMIT < maxPages) {
        val a = context.actorOf(Props[Parser](new Parser(link)))
        ongoing += (link -> a)
        LIMIT += 1
      }
    }
    if (ongoing.isEmpty) {
      context.stop(self)
      context.system.terminate()
    }
  }
}


