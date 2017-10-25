package com.bogab.akkrawler

import akka.actor.{ActorSystem, Props}
import com.bogab.akkrawler.{Dispatcher, StartCrawl}

object Akkrawler extends App {
  var seed = ""
  var maxPages = 0
  args.sliding(2, 2).toList.collect {
    case Array("--seed", argSeed: String) => seed = argSeed
    case Array("--maxPages", argMaxPages: String) => maxPages = argMaxPages.toInt
  }

  val system = ActorSystem("Akkrawler")
  val dispatcher = system.actorOf(Props[Dispatcher](new Dispatcher(seed, maxPages)))
  dispatcher ! StartCrawl
}
